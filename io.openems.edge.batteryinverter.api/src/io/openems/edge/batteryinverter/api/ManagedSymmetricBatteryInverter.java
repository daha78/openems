package io.openems.edge.batteryinverter.api;

import org.osgi.annotation.versioning.ProviderType;

import io.openems.common.channel.AccessMode;
import io.openems.common.exceptions.OpenemsError.OpenemsNamedException;
import io.openems.edge.common.channel.Doc;
import io.openems.edge.common.modbusslave.ModbusSlaveNatureTable;

@ProviderType
public interface ManagedSymmetricBatteryInverter extends SymmetricBatteryInverter {

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
	 * Apply the calculated Power.
	 * 
	 * <p>
	 * Careful: do not adjust activePower and reactivePower in this method, e.g.
	 * setting it to zero on error. The purpose of this method is solely to apply
	 * the calculated power to the ESS. If you need to constrain the allowed power,
	 * add Constraints using the {@link #getStaticConstraints()} method.
	 * 
	 * @param activePower   the active power in [W]
	 * @param reactivePower the reactive power in [var]
	 * @throws OpenemsNamedException on error; causes activation of
	 *                               APPLY_POWER_FAILED StateChannel
	 */
	public void applyPower(int activePower, int reactivePower) throws OpenemsNamedException;

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
