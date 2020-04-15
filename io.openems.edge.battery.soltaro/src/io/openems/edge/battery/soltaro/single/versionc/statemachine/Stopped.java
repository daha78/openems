package io.openems.edge.battery.soltaro.single.versionc.statemachine;

import io.openems.edge.common.statemachine.StateHandler;

public class Stopped extends StateHandler<State, Context> {

	@Override
	public State getNextState(Context context) {
		return State.UNDEFINED;
	}

}
