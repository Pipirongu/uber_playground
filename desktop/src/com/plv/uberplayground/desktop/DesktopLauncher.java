package com.plv.uberplayground.desktop;

import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Application;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3ApplicationConfiguration;
import com.plv.uberplayground.UberPlayground;

public class DesktopLauncher {
	public static void main (String[] arg) {
		Lwjgl3ApplicationConfiguration config = new Lwjgl3ApplicationConfiguration();
		config.setWindowedMode(400, 240);
		config.setTitle("Uber-Playground");
		new Lwjgl3Application(new UberPlayground(), config);
	}
}
