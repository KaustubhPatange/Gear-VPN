package com.kpstv.vpn.extensions.utils

import org.json.JSONObject

object FlagUtils {

  /**
   * Returns the url of flag image for the corresponding name of country/language.
   */
  fun getOrNull(name: String): String? {
    if (flagObject.has(name)) {
      return flagObject.getString(name)
    }
    return null
  }

  fun getAsCountryShortForms(country: String) : String {
    if (country.contains("\\s".toRegex())) {
      return country.split("\\s".toRegex()).joinToString { it.first().toString() }
    }
    return country
  }

  private const val FlagJson: String = """
    {
      "Korea": "https://cdn.countryflags.com/thumbs/south-korea/flag-round-250.png",
      "United States": "https://cdn1.iconfinder.com/data/icons/flags-of-the-world-2/128/united-states-circle-256.png",
      "United Kingdom": "https://image.flaticon.com/icons/png/512/197/197374.png",
      "Japan": "https://image.flaticon.com/icons/png/512/197/197604.png",
      "Malaysia": "https://image.flaticon.com/icons/png/512/197/197581.png",
      "France": "https://image.flaticon.com/icons/png/512/197/197560.png",
      "Germany": "https://image.flaticon.com/icons/png/512/197/197571.png",
      "Spain": "https://image.flaticon.com/icons/png/512/197/197593.png",
      "China": "https://image.flaticon.com/icons/png/512/197/197375.png",
      "Italy": "https://image.flaticon.com/icons/png/512/197/197626.png",
      "Brazil": "https://image.flaticon.com/icons/png/512/197/197386.png",
      "Russia": "https://image.flaticon.com/icons/png/512/197/197408.png",
      "Russian": "https://image.flaticon.com/icons/png/512/197/197408.png",
      "Canada": "https://image.flaticon.com/icons/png/512/197/197430.png",
      "Australia": "https://image.flaticon.com/icons/png/512/197/197507.png",
      "India": "https://image.flaticon.com/icons/png/512/197/197419.png",
      "Thailand": "https://image.flaticon.com/icons/png/512/197/197452.png",
      "Singapore": "https://image.flaticon.com/icons/png/512/197/197496.png",
      "Vietnam": "https://image.flaticon.com/icons/png/512/197/197473.png",
      "Viet Nam": "https://image.flaticon.com/icons/png/512/197/197473.png",
      "Switzerland": "https://image.flaticon.com/icons/png/512/197/197540.png",
      "Sweden": "https://image.flaticon.com/icons/png/512/197/197564.png",
      "Finland": "https://image.flaticon.com/icons/png/512/197/197585.png",
      "Mexico": "https://image.flaticon.com/icons/png/512/197/197397.png",
      "Georgia": "https://image.flaticon.com/icons/png/512/197/197380.png",
      "Netherlands": "https://image.flaticon.com/icons/png/512/197/197441.png",
      "Turkey": "https://image.flaticon.com/icons/png/512/197/197518.png",
      "Belgium": "https://image.flaticon.com/icons/png/512/197/197583.png",
      "Poland": "https://image.flaticon.com/icons/png/512/197/197529.png",
      "Portugal": "https://image.flaticon.com/icons/png/512/197/197463.png",
      "Argentina": "https://image.flaticon.com/icons/png/512/197/197573.png",
      "Chile": "https://image.flaticon.com/icons/png/512/197/197586.png",
      "Indonesia": "https://image.flaticon.com/icons/png/512/197/197559.png",
      "Colombia": "https://image.flaticon.com/icons/png/512/197/197575.png",
      "Romania": "https://image.flaticon.com/icons/png/512/197/197587.png",
      "Israel": "https://image.flaticon.com/icons/png/512/197/197577.png",
      "Taiwan": "https://image.flaticon.com/icons/png/512/197/197557.png",
      "Hong Kong": "https://image.flaticon.com/icons/png/512/197/197570.png",
      "Denmark": "https://image.flaticon.com/icons/png/512/197/197565.png",
      "Philippines": "https://image.flaticon.com/icons/png/512/197/197561.png",
      "Austria": "https://image.flaticon.com/icons/png/512/197/197447.png",
      "Peru": "https://image.flaticon.com/icons/png/512/197/197563.png",
      "South Africa": "https://image.flaticon.com/icons/png/512/197/197562.png",
      "Ukraine": "https://image.flaticon.com/icons/png/512/197/197572.png",
      "Norway": "https://image.flaticon.com/icons/png/512/197/197579.png",
      "Czech Republic": "https://image.flaticon.com/icons/png/512/197/197576.png",
      "Greece": "https://image.flaticon.com/icons/png/512/197/197566.png",
      "Ireland": "https://image.flaticon.com/icons/png/512/197/197567.png",
      "England": "https://image.flaticon.com/icons/png/512/197/197485.png",
      "Jamaica": "https://image.flaticon.com/icons/png/512/197/197611.png",
      "New Zealand": "https://image.flaticon.com/icons/png/512/197/197589.png",
      "Hungary": "https://image.flaticon.com/icons/png/512/197/197584.png",
      "Egypt": "https://image.flaticon.com/icons/png/512/197/197558.png",
      "Iceland": "https://image.flaticon.com/icons/png/512/197/197596.png",
      "Lithuania": "https://cdn.countryflags.com/thumbs/lithuania/flag-round-250.png",
      "Bangladesh": "https://cdn.countryflags.com/thumbs/bangladesh/flag-round-250.png",
      "Iraq": "https://cdn.countryflags.com/thumbs/iraq/flag-round-250.png",
      "Iran": "https://cdn.countryflags.com/thumbs/iran/flag-round-250.png",
      "Qatar": "https://cdn.countryflags.com/thumbs/qatar/flag-round-250.png",
      "Venezuela": "https://cdn.countryflags.com/thumbs/venezuela/flag-round-250.png",
      "Cambodia": "https://cdn.countryflags.com/thumbs/cambodia/flag-round-250.png",
      "Slovakia": "https://cdn.countryflags.com/thumbs/slovakia/flag-round-250.png",
      "Slovenia": "https://cdn.countryflags.com/thumbs/slovenia/flag-round-250.png",
      "Kazakhstan": "https://cdn.countryflags.com/thumbs/kazakhstan/flag-round-250.png",
      "Myanmar": "https://cdn.countryflags.com/thumbs/myanmar/flag-round-250.png",
      "Pakistan": "https://cdn.countryflags.com/thumbs/pakistan/flag-round-250.png",
      "Bulgaria": "https://cdn.countryflags.com/thumbs/bulgaria/flag-round-250.png",
      "United Arab Emirates": "https://cdn.countryflags.com/thumbs/united-arab-emirates/flag-round-250.png"
    }
  """
  private val flagObject = JSONObject(FlagJson)
}