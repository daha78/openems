package io.openems.edge.batteryinverter.api;

import org.osgi.annotation.versioning.ProviderType;

import io.openems.common.channel.AccessMode;
import io.openems.common.exceptions.OpenemsError.OpenemsNamedException;
import io.openems.edge.common.channel.Doc;
import io.openems.edge.common.modbusslave.ModbusSlaveNatureTable;

@ProviderType
public interface ManagedSymmetricBatteryInverter extends SymmetricBatteryInverter {

	public static class BatteryInverterData {
		public final Integer soc;
		public final Integer soh;
		public final Integer maxCellTemperature;
		public final Integer dischargeMinVoltage;
		public final Integer chargeMaxVoltage;
		public final Integer dischargeMaxCurrent;
		public final Integer chargeMaxCurrent;
		public final Integer setActivePower;
		public final Integer setReactivePower;

		public BatteryInverterData(Integer soc, Integer soh, Integer maxCellTemperature, Integer dischargeMinVoltage,
				Integer chargeMaxVoltage, Integer dischargeMaxCurrent, Integer chargeMaxCurrent, Integer setActivePower,
				Integer setReactivePower) {
			this.soc = soc;
			this.soh = soh;
			this.maxCellTemperature = maxCellTemperature;
			this.dischargeMinVoltage = dischargeMinVoltage;
			this.chargeMaxVoltage = chargeMaxVoltage;
			this.dischargeMaxCurrent = dischargeMaxCurrent;
			this.chargeMaxCurrent = chargeMaxCurrent;
			this.setActivePower = setActivePower;
			this.setReactivePower = setReactivePower;
		}

	}

	public enum ChannelId implements io.openems.edge.common.channel.ChannelId {
		;

		private final Doc doc;

		private ChannelId(Doc doc) {
			this.doc = doc;
		}

		public Doc doc() {
			return this.doc;
		}
	}

	public static ModbusSlaveNatureTable getModbusSlaveNatureTable(AccessMode accessMode) {
		return ModbusSlaveNatureTable.of(ManagedSymmetricBatteryInverter.class, accessMode, 100) //
				.build();
	}

	/**
	 * Apply the {@link BatteryInverterData}.
	 * 
	 * @param data the {@link BatteryInverterData}
	 * @throws OpenemsNamedException on error
	 */
	public void apply(BatteryInverterData data) throws OpenemsNamedException;

	/**
	 * Gets the smallest positive power that can be set (in W, VA or var). Example:
	 * <ul>
	 * <li>FENECON Commercial 40 allows setting of power in 100 W steps. It should
	 * return 100.
	 * <li>KACO blueplanet gridsave 50 allows setting of power in 0.1 % of 52 VA. It
	 * should return 52 (= 52000 * 0.001)
	 * </ul>
	 * 
	 * @return the power precision
	 */
	public int getPowerPrecision();

}
