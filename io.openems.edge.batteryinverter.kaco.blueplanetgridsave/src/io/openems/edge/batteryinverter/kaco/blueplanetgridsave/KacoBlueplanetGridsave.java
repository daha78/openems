package io.openems.edge.batteryinverter.kaco.blueplanetgridsave;

import java.util.Map;

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

import com.google.common.collect.ImmutableMap;

import io.openems.common.exceptions.OpenemsError.OpenemsNamedException;
import io.openems.edge.batteryinverter.api.ManagedSymmetricBatteryInverter;
import io.openems.edge.batteryinverter.api.SymmetricBatteryInverter;
import io.openems.edge.batteryinverter.sunspec.AbstractSunSpecBatteryInverter;
import io.openems.edge.bridge.modbus.api.BridgeModbus;
import io.openems.edge.bridge.modbus.api.ElementToChannelConverter;
import io.openems.edge.bridge.modbus.sunspec.ISunSpecModel;
import io.openems.edge.bridge.modbus.sunspec.SunSpecModel;
import io.openems.edge.common.component.OpenemsComponent;
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

	private final static int UNIT_ID = 1;
	private final static int READ_FROM_MODBUS_BLOCK = 1;

	/**
	 * Active SunSpec models for KACO blueplanet gridsave. Commented models are
	 * available but not used currently.
	 */
	private static final Map<ISunSpecModel, Priority> ACTIVE_MODELS = ImmutableMap.<ISunSpecModel, Priority>builder()
			.put(SunSpecModel.S_1, Priority.LOW) //
			// .put(SunSpecModel.S_103, Priority.LOW) //
			// .put(SunSpecModel.S_113, Priority.LOW) //
			// .put(SunSpecModel.S_120, Priority.LOW) //
			// .put(SunSpecModel.S_121, Priority.LOW) //
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

	public KacoBlueplanetGridsave() {
		super(//
				ACTIVE_MODELS, //
				OpenemsComponent.ChannelId.values(), //
				SymmetricBatteryInverter.ChannelId.values(), //
				ManagedSymmetricBatteryInverter.ChannelId.values(), //
				MyChannelId.values() //
		);
		this.channel(SymmetricBatteryInverter.ChannelId.GRID_MODE).setNextValue(GridMode.ON_GRID);
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
	protected void onSunSpecInitializationCompleted() {
		this.logInfo(this.log, "SunSpec initialization finished. " + this.channels().size() + " Channels available.");

		// Map SunSpec-Registers to my Channels

		// SunSpec S103
		this.mapFirstPointToChannel(//
				MyChannelId.VENDOR_OPERATING_STATE, //
				ElementToChannelConverter.DIRECT_1_TO_1, //
				SunSpecModel.S103.ST_VND);

//		this.mapFirstPointToChannel(//
//				MyChannelId.AC_ENERGY, //
//				ElementToChannelConverter.DIRECT_1_TO_1, //
//				SunSpecModel.S103.WH);
//		this.mapFirstPointToChannel(//
//				MyChannelId.DC_CURRENT, //
//				ElementToChannelConverter.DIRECT_1_TO_1, //
//				SunSpecModel.S103.DCA);
//		this.mapFirstPointToChannel(//
//				MyChannelId.DC_VOLTAGE, //
//				ElementToChannelConverter.DIRECT_1_TO_1, //
//				SunSpecModel.S103.DCV);
//		this.mapFirstPointToChannel(//
//				MyChannelId.DC_POWER, //
//				ElementToChannelConverter.DIRECT_1_TO_1, //
//				SunSpecModel.S103.DCW);
//
//		// SunSpec 64201
//		this.mapFirstPointToChannel(//
//				MyChannelId.VERSION_MAJOR, //
//				ElementToChannelConverter.DIRECT_1_TO_1, //
//				KacoSunSpecModel.S64201.VERSION);
//		this.mapFirstPointToChannel(//
//				MyChannelId.VERSION_MINOR, //
//				ElementToChannelConverter.DIRECT_1_TO_1, //
//				KacoSunSpecModel.S64201.VER_MINOR);

//		/*
//		 * SymmetricMeter
//		 */

//		this.mapFirstPointToChannel(//
//				SymmetricMeter.ChannelId.ACTIVE_POWER, //
//				ElementToChannelConverter.DIRECT_1_TO_1, //
//				SunSpecModel.S111.W, SunSpecModel.S112.W, SunSpecModel.S113.W, SunSpecModel.S101.W, SunSpecModel.S102.W,
//				SunSpecModel.S103.W);
//		this.mapFirstPointToChannel(//
//				SymmetricMeter.ChannelId.REACTIVE_POWER, //
//				ElementToChannelConverter.DIRECT_1_TO_1, //
//				SunSpecModel.S111.V_AR, SunSpecModel.S112.V_AR, SunSpecModel.S113.V_AR, SunSpecModel.S101.V_AR,
//				SunSpecModel.S102.V_AR, SunSpecModel.S103.V_AR);
//		this.mapFirstPointToChannel(//
//				SymmetricMeter.ChannelId.ACTIVE_PRODUCTION_ENERGY, //
//				ElementToChannelConverter.DIRECT_1_TO_1, //
//				SunSpecModel.S111.WH, SunSpecModel.S112.WH, SunSpecModel.S113.WH, SunSpecModel.S101.WH,
//				SunSpecModel.S102.WH, SunSpecModel.S103.WH);
//		this.mapFirstPointToChannel(//
//				SymmetricMeter.ChannelId.VOLTAGE, //
//				ElementToChannelConverter.SCALE_FACTOR_3, //
//				SunSpecModel.S111.PH_VPH_A, SunSpecModel.S111.PH_VPH_B, SunSpecModel.S111.PH_VPH_C,
//				SunSpecModel.S112.PH_VPH_A, SunSpecModel.S112.PH_VPH_B, SunSpecModel.S112.PH_VPH_C,
//				SunSpecModel.S113.PH_VPH_A, SunSpecModel.S113.PH_VPH_B, SunSpecModel.S113.PH_VPH_C,
//				SunSpecModel.S101.PH_VPH_A, SunSpecModel.S101.PH_VPH_B, SunSpecModel.S101.PH_VPH_C,
//				SunSpecModel.S102.PH_VPH_A, SunSpecModel.S102.PH_VPH_B, SunSpecModel.S102.PH_VPH_C,
//				SunSpecModel.S103.PH_VPH_A, SunSpecModel.S103.PH_VPH_B, SunSpecModel.S103.PH_VPH_C);
//		this.mapFirstPointToChannel(//
//				SymmetricMeter.ChannelId.CURRENT, //
//				ElementToChannelConverter.SCALE_FACTOR_3, //
//				SunSpecModel.S111.A, SunSpecModel.S112.A, SunSpecModel.S113.A, SunSpecModel.S101.A, SunSpecModel.S102.A,
//				SunSpecModel.S103.A);
//		this.mapFirstPointToChannel(//
//				ManagedSymmetricPvInverter.ChannelId.MAX_APPARENT_POWER, //
//				ElementToChannelConverter.DIRECT_1_TO_1, //
//				SunSpecModel.S120.W_RTG);
	}

	@Override
	protected ISunSpecModel getSunSpecModel(int blockId) throws IllegalArgumentException {
		return KacoSunSpecModel.valueOf("S_" + blockId);
	}

//	/**
//	 * Adds SunSpec block "Bidirectional inverter control backend".
//	 * 
//	 * @param startAddress the Start-Address
//	 */
//	private void addBlock64201(int startAddress) {
//		new FC3ReadRegistersTask(startAddress + 35, Priority.HIGH,
//				m(SymmetricBatteryInverter.ChannelId.ACTIVE_POWER, new SignedWordElement(startAddress + 35),
//						new ElementToChannelScaleFactorConverter(this, scaleFactorPoint.getChannelId()), //
//				m(SymmetricEss.ChannelId.REACTIVE_POWER, new SignedWordElement(SUNSPEC_64201 + 36),
//						ElementToChannelConverter.SCALE_FACTOR_1)), //
//	}

	@Override
	public void applyPower(int activePower, int reactivePower) throws OpenemsNamedException {
		this.logInfo(this.log, "apply power..."); // TODO
	}

	@Override
	public int getPowerPrecision() {
		return 1; // TODO
	}
}
