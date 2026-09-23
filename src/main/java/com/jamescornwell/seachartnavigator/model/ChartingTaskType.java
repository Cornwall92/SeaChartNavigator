package com.jamescornwell.seachartnavigator.model;

/** The activity used to complete a sea charting task. */
public enum ChartingTaskType
{
	GENERIC("Generic"),
	SPYGLASS("Spyglass"),
	CURRENT_DUCK("Current duck"),
	DRINK_CRATE("Crate"),
	MERMAID_GUIDE("Diving"),
	WEATHER("Weather");

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
