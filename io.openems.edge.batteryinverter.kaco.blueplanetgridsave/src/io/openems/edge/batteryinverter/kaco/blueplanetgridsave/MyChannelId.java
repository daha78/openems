package io.openems.edge.batteryinverter.kaco.blueplanetgridsave;

import io.openems.common.channel.Unit;
import io.openems.common.types.OpenemsType;
import io.openems.edge.common.channel.Doc;

public enum MyChannelId implements io.openems.edge.common.channel.ChannelId {
//	/*
//	 * DEBUG
//	 */
//	DEBUG_REQUESTED_STATE(Doc.of(OpenemsType.INTEGER)),
	/*
	 * SUNSPEC_103
	 */
	// see error codes in user manual "10.10 Troubleshooting" (page 48)
	VENDOR_OPERATING_STATE(Doc.of(ErrorCode.values())),
	
	
	
	
//	W_MAX(Doc.of(OpenemsType.INTEGER) //
//			.unit(Unit.WATT)), //
//	AC_ENERGY(Doc.of(OpenemsType.LONG) //
//			.unit(Unit.WATT_HOURS)), //
//	DC_CURRENT(Doc.of(OpenemsType.INTEGER) //
//			.unit(Unit.MILLIAMPERE)), //
//	DC_VOLTAGE(Doc.of(OpenemsType.INTEGER) //
//			.unit(Unit.VOLT)), //
//	DC_POWER(Doc.of(OpenemsType.INTEGER) //
//			.unit(Unit.WATT)), //
	/*
	 * SUNSPEC_64201
	 */
//	VERSION_MAJOR(Doc.of(OpenemsType.INTEGER)), //
//	VERSION_MINOR(Doc.of(OpenemsType.INTEGER)), //

//	REQUESTED_STATE(Doc.of(RequestedState.values()) //
//			.accessMode(AccessMode.WRITE_ONLY) //
//			.onInit(new EnumWriteChannel.MirrorToDebugChannel(MyChannelId.DEBUG_REQUESTED_STATE))),
//	CURRENT_STATE(Doc.of(CurrentState.values())), //
//	WATCHDOG(Doc.of(OpenemsType.INTEGER).unit(Unit.SECONDS) //
//			.accessMode(AccessMode.WRITE_ONLY)),
//	W_SET_PCT(Doc.of(OpenemsType.INTEGER).unit(Unit.PERCENT) //
//			.accessMode(AccessMode.WRITE_ONLY)),
//
//	W_SET_PCT_SF(Doc.of(OpenemsType.INTEGER) //
//			.unit(Unit.NONE)), //
//	/*
//	 * SUNSPEC_64202
//	 */
//	V_SF(Doc.of(OpenemsType.INTEGER) //
//			.unit(Unit.NONE)), //
//	A_SF(Doc.of(OpenemsType.INTEGER) //
//			.unit(Unit.NONE)), //
//	DEBUG_DIS_MIN_V(Doc.of(OpenemsType.INTEGER) //
//			.unit(Unit.VOLT)), //
//	DIS_MIN_V(new IntegerDoc() //
//			.unit(Unit.VOLT) //
//			.accessMode(AccessMode.WRITE_ONLY)
//			.onInit(new IntegerWriteChannel.MirrorToDebugChannel(MyChannelId.DEBUG_DIS_MIN_V))),
//
//	DEBUG_DIS_MAX_A(Doc.of(OpenemsType.INTEGER) //
//			.unit(Unit.AMPERE)), //
//	DIS_MAX_A(new IntegerDoc() //
//			.unit(Unit.AMPERE) //
//			.accessMode(AccessMode.WRITE_ONLY)
//			.onInit(new IntegerWriteChannel.MirrorToDebugChannel(MyChannelId.DEBUG_DIS_MAX_A))),
//	// DIS_CUTOFF_A(Doc.of(OpenemsType.INTEGER) //
//	// .text("Disconnect if discharge current lower than DisCutoffA")),
//	// TODO scale factor
//	DEBUG_CHA_MAX_V(Doc.of(OpenemsType.INTEGER) //
//			.unit(Unit.VOLT)), //
//	CHA_MAX_V(new IntegerDoc() //
//			.unit(Unit.VOLT) //
//			.accessMode(AccessMode.WRITE_ONLY)
//			.onInit(new IntegerWriteChannel.MirrorToDebugChannel(MyChannelId.DEBUG_CHA_MAX_V))),
//	DEBUG_CHA_MAX_A(Doc.of(OpenemsType.INTEGER) //
//			.unit(Unit.AMPERE)), //
//	CHA_MAX_A(new IntegerDoc() //
//			.unit(Unit.AMPERE) //
//			.accessMode(AccessMode.WRITE_ONLY)
//			.onInit(new IntegerWriteChannel.MirrorToDebugChannel(MyChannelId.DEBUG_CHA_MAX_A))),
//	// CHA_CUTOFF_A(Doc.of(OpenemsType.INTEGER) //
//	// .text("Disconnect if charge current lower than ChaCuttoffA")),
//	// TODO scale factor
//	DEBUG_EN_LIMIT(Doc.of(OpenemsType.INTEGER)), //
//	EN_LIMIT(new IntegerDoc() //
//			.text("new battery limits are activated when EnLimit is 1") //
//			.accessMode(AccessMode.WRITE_ONLY)
//			.onInit(new IntegerWriteChannel.MirrorToDebugChannel(MyChannelId.DEBUG_EN_LIMIT))),
//	/*
//	 * SUNSPEC_64203
//	 */
//	SOC_SF(Doc.of(OpenemsType.INTEGER) //
//			.unit(Unit.NONE)), //
//	SOH_SF(Doc.of(OpenemsType.INTEGER) //
//			.unit(Unit.NONE)), //
//	TEMP_SF(Doc.of(OpenemsType.INTEGER) //
//			.unit(Unit.NONE)), //
//	BAT_SOC(Doc.of(OpenemsType.INTEGER).unit(Unit.PERCENT) //
//			.accessMode(AccessMode.WRITE_ONLY)),
//	BAT_SOH(Doc.of(OpenemsType.INTEGER).unit(Unit.PERCENT) //
//			.accessMode(AccessMode.WRITE_ONLY)),
//	BAT_TEMP(Doc.of(OpenemsType.INTEGER).unit(Unit.DEGREE_CELSIUS) //
//			.accessMode(AccessMode.WRITE_ONLY)),
//	/*
//	 * SUNSPEC_64302
//	 */
//	COMMAND_ID_REQ(Doc.of(OpenemsType.INTEGER).unit(Unit.NONE) //
//			.accessMode(AccessMode.WRITE_ONLY)),
//	REQ_PARAM_0(Doc.of(OpenemsType.INTEGER).unit(Unit.NONE) //
//			.accessMode(AccessMode.WRITE_ONLY)),
//	COMMAND_ID_REQ_ENA(Doc.of(OpenemsType.INTEGER).unit(Unit.NONE) //
//			.accessMode(AccessMode.WRITE_ONLY)),
//	COMMAND_ID_RES(Doc.of(OpenemsType.INTEGER) //
//			.unit(Unit.NONE)), //
//	RETURN_CODE(Doc.of(OpenemsType.INTEGER) //
//			.unit(Unit.NONE)); //
	;

	private final Doc doc;

	private MyChannelId(Doc doc) {
		this.doc = doc;
	}

	public Doc doc() {
		return this.doc;
	}
}