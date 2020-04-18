/*
 * -------------------------------------------------------------------------------------------------------
 * Class: JsonMaterialHandler
 * This class is part of Metallurgy 4 Reforged
 * Complete source code is available at: https://github.com/Davoleo/Metallurgy-4-Reforged
 * This code is licensed under GNU GPLv3
 * Authors: ItHurtsLikeHell & Davoleo
 * Copyright (c) 2020.
 * --------------------------------------------------------------------------------------------------------
 */

package it.hurts.metallurgy_reforged.material;

import com.google.gson.*;
import it.hurts.metallurgy_reforged.Metallurgy;
import net.minecraft.util.JsonUtils;

import java.io.BufferedReader;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.*;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public class JsonMaterialHandler {

	private static final Gson gson = new GsonBuilder().setPrettyPrinting().create();

	public static final String DEFAULT_CONFIG = Metallurgy.class.getResource("/assets/metallurgy/materials.json").getPath();

	/**
	 * Reads the JSON config and creates a new MetalStats object for each entry of the JSON Array
	 *
	 * @param path         The path to the JSON Config
	 * @param defaultStats used as fallback when reading user-customized JSON config,
	 *
	 * @return
	 */
	public static Set<MetalStats> readConfig(String path, Set<MetalStats> defaultStats)
	{
		Set<MetalStats> metalStats = new HashSet<>();

		try
		{
			BufferedReader reader = Files.newBufferedReader(getPath(path));
			JsonArray materials = JsonUtils.fromJson(gson, reader, JsonArray.class);

			if (materials != null)
			{
				materials.forEach(jsonElement -> {
					JsonObject jsonMetal = jsonElement.isJsonObject() ? jsonElement.getAsJsonObject() : null;

					if (jsonMetal != null)
					{
						MetalStats metalStat = readMetalFromJson(jsonMetal, defaultStats);
						metalStats.add(metalStat);
					}
				});
			}
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}

		return metalStats;
	}

	/**
	 * @param metalObj
	 * @param defaultStats
	 *
	 * @return
	 */
	private static MetalStats readMetalFromJson(JsonObject metalObj, Set<MetalStats> defaultStats)
	{

		if (!metalObj.has("name"))
			return null;

		String name = JsonUtils.getString(metalObj, "name");

		MetalStats defaultStat = getMetalStatsByName(name, defaultStats);

		float hardness = JsonUtils.getFloat(metalObj, "hardness", defaultStat.getHardness());
		float blockBlastResistance = JsonUtils.getFloat(metalObj, "blast_resistance", defaultStat.getHardness());
		int oreHarvest = JsonUtils.getInt(metalObj, "ore_harvest_level", defaultStat.getOreHarvest());
		int color = Integer.parseInt(JsonUtils.getString(metalObj, "color", String.valueOf(defaultStat.getColorHex())));

		//TODO filter null armor and tools
		ArmorStats armorStats = getArmorStats(metalObj, defaultStat.getArmorStats());
		ToolStats toolStats = getToolStats(metalObj, defaultStat.getToolStats());

		return new MetalStats(name, hardness, blockBlastResistance, armorStats, toolStats, oreHarvest, color);
	}

	private static MetalStats getMetalStatsByName(String name, Set<MetalStats> defaultStats)
	{

		if (defaultStats != null)
		{
			for (MetalStats stat : defaultStats)
			{
				if (stat.getName().equals(name))
				{
					return stat;
				}
			}
		}

		return MetalStats.EMPTY_METAL_STATS;
	}

	private static ArmorStats getArmorStats(JsonObject metalStats, ArmorStats fallback)
	{

		if (metalStats.has("armor_stats"))
		{
			JsonObject armorStats = JsonUtils.getJsonObject(metalStats, "armor_stats");

			int enchantability = JsonUtils.getInt(armorStats, "enchantability", fallback.getEnchantability());
			int durability = JsonUtils.getInt(armorStats, "durability", fallback.getDurability());
			float toughness = JsonUtils.getFloat(armorStats, "toughness", fallback.getToughness());
			int[] damageReduction = getIntArray(armorStats, "damage_reduction", fallback.getDamageReduction());

			return new ArmorStats(damageReduction, enchantability, durability, toughness);
		}

		return fallback;
	}

	private static ToolStats getToolStats(JsonObject metalStats, ToolStats fallback)
	{

		if (metalStats.has("tool_stats"))
		{
			JsonObject toolStats = JsonUtils.getJsonObject(metalStats, "tool_stats");

			float efficiency = JsonUtils.getFloat(toolStats, "efficiency", fallback.getEfficiency());
			int harvestLevel = JsonUtils.getInt(toolStats, "harvest_level", fallback.getHarvestLevel());
			int enchantability = JsonUtils.getInt(toolStats, "enchantability", fallback.getToolMagic());
			int durability = JsonUtils.getInt(toolStats, "durability", fallback.getMaxUses());
			float damage = JsonUtils.getFloat(toolStats, "damage", fallback.getDamage());

			return new ToolStats(enchantability, harvestLevel, durability, efficiency, damage);
		}

		return fallback;
	}

	private static int[] getIntArray(JsonObject json, String memberName, int[] fallback)
	{

		int[] arr = new int[4];

		if (json.has(memberName))
		{
			JsonArray jsonArray = json.getAsJsonArray(memberName);

			for (int i = 0; i < arr.length; i++)
			{
				JsonElement element = jsonArray.get(i);
				if (JsonUtils.isNumber(element))
				{
					arr[i] = element.getAsInt();
				}
				else
				{
					arr[i] = fallback[i];
				}
			}
		}

		return arr;
	}

	private static Path getPath(String resource)
	{
		FileSystem filesystem;

		try
		{
			URL url = Metallurgy.class.getResource(resource);

			if (url != null)
			{
				URI uri = url.toURI();
				Path path;

				if ("file".equals(uri.getScheme()))
				{
					path = Paths.get(Metallurgy.class.getResource(resource).toURI());
				}
				else
				{
					filesystem = FileSystems.newFileSystem(uri, Collections.emptyMap());
					path = filesystem.getPath("/assets/minecraft/recipes");
				}

				return path;
			}
		}
		catch (URISyntaxException e)
		{
			e.printStackTrace();
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}

		return null;
	}

}