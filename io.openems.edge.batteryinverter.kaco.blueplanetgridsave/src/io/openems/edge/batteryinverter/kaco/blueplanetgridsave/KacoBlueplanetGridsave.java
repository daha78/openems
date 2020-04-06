package io.openems.edge.batteryinverter.kaco.blueplanetgridsave;

import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.Optional;

import org.osgi.service.cm.ConfigurationAdmin;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.ConfigurationPolicy;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;
import org.osgi.service.component.annotations.ReferencePolicyOption;
import org.osgi.service.metatype.annotations.Designate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Objects;
import com.google.common.collect.ImmutableMap;

import io.openems.common.exceptions.OpenemsError.OpenemsNamedException;
import io.openems.common.exceptions.OpenemsException;
import io.openems.edge.batteryinverter.api.ManagedSymmetricBatteryInverter;
import io.openems.edge.batteryinverter.api.SymmetricBatteryInverter;
import io.openems.edge.batteryinverter.kaco.blueplanetgridsave.KacoSunSpecModel.S64201.S64201_CurrentState;
import io.openems.edge.batteryinverter.kaco.blueplanetgridsave.KacoSunSpecModel.S64201.S64201_RequestedState;
import io.openems.edge.batteryinverter.kaco.blueplanetgridsave.KacoSunSpecModel.S64202.S64202_EnLimit;
import io.openems.edge.batteryinverter.sunspec.AbstractSunSpecBatteryInverter;
import io.openems.edge.bridge.modbus.api.BridgeModbus;
import io.openems.edge.bridge.modbus.sunspec.ISunSpecModel;
import io.openems.edge.bridge.modbus.sunspec.SunSpecModel;
import io.openems.edge.common.channel.EnumReadChannel;
import io.openems.edge.common.channel.EnumWriteChannel;
import io.openems.edge.common.channel.IntegerReadChannel;
import io.openems.edge.common.channel.IntegerWriteChannel;
import io.openems.edge.common.channel.value.Value;
import io.openems.edge.common.component.OpenemsComponent;
import io.openems.edge.common.cycle.Cycle;
import io.openems.edge.common.sum.GridMode;
import io.openems.edge.common.taskmanager.Priority;

@Designate(ocd = Config.class, factory = true)
@Component(//
		name = "Battery-Inverter.KACO.BlueplanetGridsave", //
		immediate = true, //
		configurationPolicy = ConfigurationPolicy.REQUIRE //
)
public class KacoBlueplanetGridsave extends AbstractSunSpecBatteryInverter
		implements ManagedSymmetricBatteryInverter, SymmetricBatteryInverter, OpenemsComponent {

	private static final int UNIT_ID = 1;
	private static final int READ_FROM_MODBUS_BLOCK = 1;
	private static final int WATCHDOG_CYCLES = 10;

	private final Cycle cycle;

	/**
	 * Was SunSpec initialization completed? This takes some time at startup.
	 */
	private boolean isSunSpecInitializationFinished = false;

	/**
	 * Active SunSpec models for KACO blueplanet gridsave. Commented models are
	 * available but not used currently.
	 */
	private static final Map<ISunSpecModel, Priority> ACTIVE_MODELS = ImmutableMap.<ISunSpecModel, Priority>builder()
			.put(SunSpecModel.S_1, Priority.LOW) //
			// .put(SunSpecModel.S_103, Priority.LOW) //
			// .put(SunSpecModel.S_113, Priority.LOW) //
			// .put(SunSpecModel.S_120, Priority.LOW) //
			.put(SunSpecModel.S_121, Priority.LOW) //
			// .put(SunSpecModel.S_122, Priority.LOW) //
			// .put(SunSpecModel.S_123, Priority.LOW) //
			// .put(SunSpecModel.S_126, Priority.LOW) //
			// .put(SunSpecModel.S_129, Priority.LOW) //
			// .put(SunSpecModel.S_130, Priority.LOW) //
			// .put(SunSpecModel.S_132, Priority.LOW) //
			// .put(SunSpecModel.S_135, Priority.LOW) //
			// .put(SunSpecModel.S_136, Priority.LOW) //
			// .put(SunSpecModel.S_160, Priority.LOW) //
			.put(KacoSunSpecModel.S_64201, Priority.LOW) //
			.put(KacoSunSpecModel.S_64202, Priority.LOW) //
			.put(KacoSunSpecModel.S_64203, Priority.LOW) //
			.put(KacoSunSpecModel.S_64204, Priority.LOW) //
			.build();

	private final Logger log = LoggerFactory.getLogger(KacoBlueplanetGridsave.class);

	@Reference
	protected ConfigurationAdmin cm;

	@Activate
	public KacoBlueplanetGridsave(@Reference Cycle cycle) {
		super(//
				ACTIVE_MODELS, //
				OpenemsComponent.ChannelId.values(), //
				SymmetricBatteryInverter.ChannelId.values(), //
				ManagedSymmetricBatteryInverter.ChannelId.values(), //
				MyChannelId.values() //
		);
		this.channel(SymmetricBatteryInverter.ChannelId.GRID_MODE).setNextValue(GridMode.ON_GRID);
		this.cycle = cycle;
	}

	@Reference(policy = ReferencePolicy.STATIC, policyOption = ReferencePolicyOption.GREEDY, cardinality = ReferenceCardinality.MANDATORY)
	protected void setModbus(BridgeModbus modbus) {
		super.setModbus(modbus);
	}

	@Activate
	void activate(ComponentContext context, Config config) {
		super.activate(context, config.id(), config.alias(), config.enabled(), UNIT_ID, this.cm, "Modbus",
				config.modbus_id(), READ_FROM_MODBUS_BLOCK);
	}

	@Deactivate
	protected void deactivate() {
		super.deactivate();
	}

	@Override
	public void apply(BatteryInverterData data) throws OpenemsNamedException {
		if (!this.isSunSpecInitializationFinished) {
			return;
		}

		// Set Display Information
		this.setDisplayInformation(data);

		// Set Battery Limits
		this.setBatteryLimits(data);

		// Act according to the State-Machine
		this.handleStateMachine();

		// Set Max Apparent Power
		this.setMaxApparentPower();

		// Apply Active and Reactive Power Set-Points
		this.applyPower(data);

		// Trigger Watchdog
		this.triggerWatchdog();
	}

	/**
	 * Applies the Active and Reactive Power Set-Points.
	 * 
	 * @param data the {@link BatteryInverterData}
	 * @throws OpenemsNamedException on error
	 */
	private void applyPower(BatteryInverterData data) throws OpenemsNamedException {
		// TODO apply reactive power
		IntegerWriteChannel wSetPctChannel = this.getSunSpecChannelOrError(KacoSunSpecModel.S64201.W_SET_PCT);
		IntegerReadChannel maxApparentPowerChannel = this
				.channel(SymmetricBatteryInverter.ChannelId.MAX_APPARENT_POWER);
		int maxApparentPower = maxApparentPowerChannel.value().getOrError();

		// Active Power Set-Point is set in % of maximum active power
		int wSetPct = data.setActivePower * 100 / maxApparentPower;
		wSetPctChannel.setNextWriteValue(wSetPct);
	}

	/**
	 * Acts according to the State-Machine.
	 * 
	 * @throws OpenemsException on error
	 */
	private void handleStateMachine() throws OpenemsException {
		S64201_CurrentState currentState = this.getCurrentState();
		switch (currentState) {
		case OFF:
			this.doOffHandling();
			break;
		case STANDBY:
			this.doStandbyHandling();
			break;
		case FAULT:
		case NO_ERROR_PENDING:
			this.doErrorHandling();
			break;
		case GRID_CONNECTED:
		case THROTTLED:
			this.doGridConnectedHandling();
			break;
		case PRECHARGE:
		case SHUTTING_DOWN:
		case STARTING:
		case UNDEFINED:
		case GRID_PRE_CONNECTED:
		case MPPT:
		case SLEEPING:
			// Do nothing because these states are only temporarily reached
			break;
		}
	}

	/**
	 * Handles the State "OFF".
	 * 
	 * @throws OpenemsException on error
	 */
	private void doOffHandling() throws OpenemsException {
		this.setRequestedState(S64201_RequestedState.STANDBY);
	}

	/**
	 * Handles the State "STANDBY".
	 * 
	 * @throws OpenemsException on error
	 */
	private void doStandbyHandling() throws OpenemsException {
		this.setRequestedState(S64201_RequestedState.GRID_CONNECTED);
	}

	/**
	 * Handles the State "ERROR".
	 * 
	 * @throws OpenemsException on error
	 */
	private void doErrorHandling() throws OpenemsException {
		// find out the reason what is wrong an react
		// for a first try, switch system off, it will be restarted
		this.setRequestedState(S64201_RequestedState.OFF);
	}

	/**
	 * Handles the States "GRID_CONNECTED" and "THROTTLED".
	 * 
	 * @throws OpenemsException on error
	 */
	private void doGridConnectedHandling() {
		// nothing to do here
	}

	/**
	 * Sets the Battery Limits.
	 * 
	 * @param data the {@link BatteryInverterData}
	 * @throws OpenemsException on error
	 */
	private void setBatteryLimits(BatteryInverterData data) throws OpenemsException {
		// Discharge Min Voltage
		IntegerWriteChannel disMinVChannel = this.getSunSpecChannelOrError(KacoSunSpecModel.S64202.DIS_MIN_V_0);
		if (Objects.equal(data.dischargeMinVoltage, 0)) {
			disMinVChannel.setNextValue(null); // according to setup manual DIS_MIN_V must not be zero
		} else {
			disMinVChannel.setNextValue(data.dischargeMinVoltage);
		}

		// Charge Max Voltage
		IntegerWriteChannel chaMaxVChannel = this.getSunSpecChannelOrError(KacoSunSpecModel.S64202.CHA_MAX_V_0);
		if (Objects.equal(data.chargeMaxVoltage, 0)) {
			chaMaxVChannel.setNextValue(null); // according to setup manual CHA_MAX_V must not be zero
		} else {
			chaMaxVChannel.setNextValue(data.chargeMaxVoltage);
		}

		// Discharge Max Current
		IntegerWriteChannel disMaxAChannel = this.getSunSpecChannelOrError(KacoSunSpecModel.S64202.DIS_MAX_A_0);
		disMaxAChannel.setNextValue(data.dischargeMaxCurrent);

		// Charge Max Current
		IntegerWriteChannel chaMaxAChannel = this.getSunSpecChannelOrError(KacoSunSpecModel.S64202.CHA_MAX_A_0);
		chaMaxAChannel.setNextValue(data.chargeMaxCurrent);

		// Activate Battery values
		EnumWriteChannel enLimitChannel = this.getSunSpecChannelOrError(KacoSunSpecModel.S64202.EN_LIMIT_0);
		enLimitChannel.setNextValue(S64202_EnLimit.ACTIVATE);
	}

	/**
	 * Sets the information that is shown on the Display, like State-of-Charge,
	 * State-of-Health and Max-Cell-Temperature.
	 * 
	 * @param data the {@link BatteryInverterData}
	 * @throws OpenemsException on error
	 */
	private void setDisplayInformation(BatteryInverterData data) throws OpenemsException {
		// State-of-Charge
		IntegerWriteChannel batSocChannel = this.getSunSpecChannelOrError(KacoSunSpecModel.S64203.BAT_SOC_0);
		batSocChannel.setNextValue(data.soc);

		// State-of-Health
		IntegerWriteChannel batSohChannel = this.getSunSpecChannelOrError(KacoSunSpecModel.S64203.BAT_SOH_0);
		batSohChannel.setNextValue(data.soh);

		// Max-Cell-Temperature
		IntegerWriteChannel batTempChannel = this.getSunSpecChannelOrError(KacoSunSpecModel.S64203.BAT_TEMP_0);
		batTempChannel.setNextValue(data.maxCellTemperature);
	}

	/**
	 * Sets the Max-Apparent-Power. Is also used to block any Set-Points by setting
	 * max apparent power to zero.
	 * 
	 * @throws OpenemsException on error
	 */
	private void setMaxApparentPower() throws OpenemsException {
		Integer maxApparentPower = this.calculateMaxApparentPower();
		IntegerReadChannel maxApparentPowerChannel = this
				.channel(SymmetricBatteryInverter.ChannelId.MAX_APPARENT_POWER);
		maxApparentPowerChannel.setNextValue(maxApparentPower);
	}

	/**
	 * Calculates the Max-Apparent-Power. Is also used to block any Set-Points by
	 * setting max apparent power to zero.
	 */
	private Integer calculateMaxApparentPower() {
		S64201_CurrentState currentState = this.getCurrentState();
		switch (currentState) {
		case FAULT:
		case GRID_PRE_CONNECTED:
		case MPPT:
		case NO_ERROR_PENDING:
		case OFF:
		case PRECHARGE:
		case SHUTTING_DOWN:
		case SLEEPING:
		case STANDBY:
		case STARTING:
		case UNDEFINED:
			// Block any power as long as we are not in the correct states
			return 0;
		case GRID_CONNECTED:
		case THROTTLED:
			// if inverter is throttled, maybe full power is not reachable, but the device
			// is still working
			break;
		}
		// Get Max-Apparent-Power from
		Optional<IntegerReadChannel> wMaxChannel = this.getSunSpecChannel(SunSpecModel.S121.W_MAX);
		if (!wMaxChannel.isPresent()) {
			// Channel is not available
			return 0;
		}
		return wMaxChannel.get().value().get();
	}

	/**
	 * Triggers the Watchdog after half of the WATCHDOG_CYCLES passed.
	 * 
	 * @throws OpenemsException on error
	 */
	private void triggerWatchdog() throws OpenemsException {
		int watchdogSeconds = this.cycle.getCycleTime() / 1000 * WATCHDOG_CYCLES;
		if (Duration.between(this.lastTriggerWatchdog, Instant.now()).getSeconds() > watchdogSeconds / 2) {
			IntegerReadChannel watchdogChannel = this.getSunSpecChannelOrError(KacoSunSpecModel.S64201.WATCHDOG);
			watchdogChannel.setNextValue(watchdogSeconds);
			this.lastTriggerWatchdog = Instant.now();
		}
	}

	private Instant lastTriggerWatchdog = Instant.MIN;

	/**
	 * Mark SunSpec initialization completed; this takes some time at startup.
	 */
	@Override
	protected void onSunSpecInitializationCompleted() {
		this.logInfo(this.log, "SunSpec initialization finished.");
		this.isSunSpecInitializationFinished = true;
	}

	/**
	 * Sets the Requested-State.
	 * 
	 * @param requestedState the {@link S64201_RequestedState}
	 * @throws OpenemsException on error
	 */
	private void setRequestedState(S64201_RequestedState requestedState) throws OpenemsException {
		EnumWriteChannel requestedStateChannel = this.getSunSpecChannelOrError(KacoSunSpecModel.S64201.REQUESTED_STATE);
		requestedStateChannel.setNextValue(requestedState);
	}

	/**
	 * Gets the Current State.
	 * 
	 * @return the {@link S64201_CurrentState}
	 */
	private S64201_CurrentState getCurrentState() {
		Optional<EnumReadChannel> channel = this.getSunSpecChannel(KacoSunSpecModel.S64201.REQUESTED_STATE);
		if (channel.isPresent()) {
			return channel.get().value().asEnum();
		} else {
			return S64201_CurrentState.UNDEFINED;
		}
	}

	@Override
	protected ISunSpecModel getSunSpecModel(int blockId) throws IllegalArgumentException {
		return KacoSunSpecModel.valueOf("S_" + blockId);
	}

	/**
	 * Calculate the Power-Precision from the Max Apparent Power using the SetPoint
	 * scale-factor.
	 */
	@Override
	public int getPowerPrecision() {
		Optional<IntegerReadChannel> scalefactorChannel = this.getSunSpecChannel(KacoSunSpecModel.S64201.W_SET_PCT_SF);
		if (!scalefactorChannel.isPresent()) {
			return 1;
		}
		Value<Integer> scalefactor = scalefactorChannel.get().value();
		Value<Integer> maxApparentPower = this.getMaxApparentPower();
		if (!scalefactor.isDefined() || !maxApparentPower.isDefined()) {
			return 1;
		}
		return (int) (maxApparentPower.get() * 0.01 * Math.pow(10, scalefactor.get()));
	}
}
