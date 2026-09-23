package com.jamescornwell.seachartnavigator.model;

/** The activity used to complete a sea charting task. */
public enum ChartingTaskType
{
	GENERIC("Charting"),
	SPYGLASS("Spyglass"),
	CURRENT_DUCK("Current duck"),
	DRINK_CRATE("Drink crate"),
	MERMAID_GUIDE("Mermaid guide"),
	WEATHER("Weather station");

	private final String displayName;

	ChartingTaskType(String displayName)
	{
		this.displayName = displayName;
	}

	public String getDisplayName()
	{
		return displayName;
	}
}

