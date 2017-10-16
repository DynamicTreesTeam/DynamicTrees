package com.ferreusveritas.dynamictrees.seasons;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.world.World;

/**
 * Basic season provider with minimal features
 * 
 * @author ferreusveritas
 *
 */
public class SeasonProviderBasic implements ISeasonProvider {

	protected int currentDay = 0;
	protected int day;//24000 ticks(20 ticks/second)
	protected int month;//8 days
	protected long year;//8 months
	protected float season;//Each season is 2 months or 1/4 year
	
	//Some bullshit greek inspired month names
	public static final String monthNames[] = {"Monostus", "Diastus", "Triastus", "Tetrastus", "Pentastus", "Hexastus", "Heptastus", "Octastus"};
	public static final String numeralSuffix[] = {"th", "st", "nd", "rd", "th", "th", "th", "th", "th", "th"};
	public static final String seasonNames[] = {"Spring", "Summer", "Fall", "Winter"};
	
	int monthsPerYear = 8;//Set to 8 because it's a nice power of two number that makes each season 2 months long
	int daysPerMonth = 8;//Set to 8 because WorldProvider.getMoonPhase returns 0-7.  Therefore a Minecraft month is 8 days. Deal with it.
	final int ticksPerDay = 24000;//This is pretty well known and standard.

	public SeasonProviderBasic() {
	}

	public SeasonProviderBasic(int monthsPerYear, int daysPerMonth) {
		this.monthsPerYear = monthsPerYear;
		this.daysPerMonth = daysPerMonth;
	}
	
	@Override
	public void updateTick(World world, long worldTicks) {
		
		year = worldTicks / (ticksPerDay * daysPerMonth * monthsPerYear); //years since epoch
		month = (int) ((worldTicks / (ticksPerDay * daysPerMonth)) % monthsPerYear); //month of the year
		day = (int) ((worldTicks / ticksPerDay) % daysPerMonth); //day of the month
		season = ((month * daysPerMonth) + day) / ((daysPerMonth * monthsPerYear) / 4.0f);

		if(currentDay != day) {
			notifyPlayersOfDate(world);
			currentDay = day;
		}
		
	}

	public void notifyPlayersOfDate(World world) {
		
		String dayString = String.valueOf(day + 1) + numeralSuffix[(day + 1) % 10]; 
		String dataString = dayString + " of " + monthNames[month] + " " + String.valueOf(year) + " S:" + seasonNames[(int) Math.floor(season)] + " [" + String.format("%1.2f", season) + "] M:" + world.getMoonPhase() + " V:" + world.getWorldTime();
		
		if(!world.isRemote) {
			for(EntityPlayer player : world.playerEntities) {
				player.addChatMessage(new TextComponentString("Date: " + dataString));
			}
		}
	}
	
	//0 Spring
	//1 Summer
	//2 Fall
	//3 Winter
	@Override
	public float getSeasonValue() {
		return season;
	}

	public int getDayOfMonth() {
		return day;
	}

	public int getMonth() {
		return month;
	}

	public long getYear() {
		return year;
	}

}