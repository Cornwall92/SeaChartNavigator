package com.jamescornwell.seachartnavigator;

/** Controls when the movable on-screen navigation HUD is drawn. */
public enum HudVisibility
{
	WHEN_SAILING("Only while sailing"),
	ALWAYS("Always show"),
	HIDDEN("Hide HUD");

	private final String displayName;

	HudVisibility(String displayName)
	{
		this.displayName = displayName;
	}

	@Override
	public String toString()
	{
		return displayName;
	}
}
