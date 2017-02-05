package com.plv.uberplayground.config;

public class Configuration {

	public final static float PPM = 32f;
	public static float ElapsedTime = 0;

	public static enum EntityCategory {
		BOUNDARY((short) 0x1), PLAYER((short) 0x2), ENEMY_SHIP((short) 0x3), CONTROL_POINT((short) 0x4);

		private final short id;

		EntityCategory(short id) {
			this.id = id;
		}

		public short getValue() {
			return this.id;
		}
	};
}
