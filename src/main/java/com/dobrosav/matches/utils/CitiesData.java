package com.dobrosav.matches.utils;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CitiesData {

    public static final Map<String, List<String>> CITIES_BY_COUNTRY = new HashMap<>();

    static {
        CITIES_BY_COUNTRY.put("United States", Arrays.asList("New York", "Los Angeles", "Chicago", "Houston", "Phoenix", "Philadelphia", "San Antonio", "San Diego", "Dallas", "San Jose", "Austin", "Jacksonville", "Fort Worth", "Columbus", "San Francisco", "Charlotte", "Indianapolis", "Seattle", "Denver", "Washington", "Boston", "El Paso", "Nashville", "Detroit", "Oklahoma City", "Portland", "Las Vegas", "Memphis", "Louisville", "Baltimore", "Milwaukee", "Albuquerque", "Tucson", "Fresno", "Mesa", "Sacramento", "Atlanta", "Kansas City", "Colorado Springs", "Miami", "Raleigh", "Omaha", "Long Beach", "Virginia Beach", "Oakland", "Minneapolis", "Tulsa", "Tampa", "Arlington", "New Orleans"));
        CITIES_BY_COUNTRY.put("China", Arrays.asList("Shanghai", "Beijing", "Guangzhou", "Shenzhen", "Tianjin", "Wuhan", "Dongguan", "Chongqing", "Chengdu", "Nanjing", "Nanchong", "Xi'an", "Shenyang", "Hangzhou", "Harbin", "Tai'an", "Suzhou", "Shantou", "Jinan", "Zhengzhou", "Changchun", "Dalian", "Kunming", "Qingdao", "Foshan", "Puyang", "Wuxi", "Xiamen", "Tianshui", "Ningbo", "Shiyan", "Taiyuan", "Tangshan", "Hefei", "Zibo", "Changsha", "Shijiazhuang", "Lanzhou", "Yunfu", "Nanchang"));
        CITIES_BY_COUNTRY.put("India", Arrays.asList("Mumbai", "Delhi", "Bangalore", "Hyderabad", "Ahmedabad", "Chennai", "Kolkata", "Surat", "Pune", "Jaipur", "Lucknow", "Kanpur", "Nagpur", "Indore", "Thane", "Bhopal", "Visakhapatnam", "Pimpri-Chinchwad", "Patna", "Vadodara", "Ghaziabad", "Ludhiana", "Agra", "Nashik", "Faridabad", "Meerut", "Rajkot", "Kalyan-Dombivli", "Vasai-Virar", "Varanasi", "Srinagar", "Aurangabad", "Dhanbad", "Amritsar", "Navi Mumbai", "Allahabad", "Howrah", "Ranchi", "Gwalior", "Jabalpur"));
        CITIES_BY_COUNTRY.put("Japan", Arrays.asList("Tokyo", "Yokohama", "Osaka", "Nagoya", "Sapporo", "Kobe", "Kyoto", "Fukuoka", "Kawasaki", "Saitama", "Hiroshima", "Sendai", "Kitakyushu", "Chiba", "Sakai", "Niigata", "Hamamatsu", "Kumamoto", "Sagamihara", "Shizuoka"));
        CITIES_BY_COUNTRY.put("Brazil", Arrays.asList("São Paulo", "Rio de Janeiro", "Brasília", "Salvador", "Fortaleza", "Belo Horizonte", "Manaus", "Curitiba", "Recife", "Goiânia", "Belém", "Porto Alegre", "Guarulhos", "Campinas", "São Luís", "São Gonçalo", "Maceió", "Duque de Caxias", "Natal", "Teresina"));
        CITIES_BY_COUNTRY.put("Russia", Arrays.asList("Moscow", "Saint Petersburg", "Novosibirsk", "Yekaterinburg", "Nizhny Novgorod", "Kazan", "Chelyabinsk", "Omsk", "Samara", "Rostov-on-Don", "Ufa", "Krasnoyarsk", "Perm", "Voronezh", "Volgograd"));
        CITIES_BY_COUNTRY.put("Mexico", Arrays.asList("Mexico City", "Ecatepec", "Guadalajara", "Puebla", "Juárez", "Tijuana", "León", "Zapopan", "Monterrey", "Nezahualcóyotl"));
        CITIES_BY_COUNTRY.put("Indonesia", Arrays.asList("Jakarta", "Surabaya", "Bandung", "Bekasi", "Medan", "Tangerang", "Depok", "Semarang", "Palembang", "South Tangerang"));
        CITIES_BY_COUNTRY.put("Germany", Arrays.asList("Berlin", "Hamburg", "Munich", "Cologne", "Frankfurt", "Stuttgart", "Düsseldorf", "Dortmund", "Essen", "Leipzig", "Bremen", "Dresden", "Hanover", "Nuremberg", "Duisburg"));
        CITIES_BY_COUNTRY.put("United Kingdom", Arrays.asList("London", "Birmingham", "Leeds", "Glasgow", "Sheffield", "Manchester", "Edinburgh", "Liverpool", "Bristol", "Cardiff"));
        CITIES_BY_COUNTRY.put("France", Arrays.asList("Paris", "Marseille", "Lyon", "Toulouse", "Nice", "Nantes", "Montpellier", "Strasbourg", "Bordeaux", "Lille"));
        CITIES_BY_COUNTRY.put("Italy", Arrays.asList("Rome", "Milan", "Naples", "Turin", "Palermo", "Genoa", "Bologna", "Florence", "Bari", "Catania"));
        CITIES_BY_COUNTRY.put("Spain", Arrays.asList("Madrid", "Barcelona", "Valencia", "Seville", "Zaragoza", "Málaga", "Murcia", "Palma", "Las Palmas", "Bilbao"));
        CITIES_BY_COUNTRY.put("Turkey", Arrays.asList("Istanbul", "Ankara", "Izmir", "Bursa", "Adana", "Gaziantep", "Konya", "Antalya", "Kayseri", "Mersin"));
        CITIES_BY_COUNTRY.put("Serbia", Arrays.asList("Belgrade", "Novi Sad", "Niš", "Kragujevac", "Subotica"));
        CITIES_BY_COUNTRY.put("Croatia", Arrays.asList("Zagreb", "Split", "Rijeka", "Osijek"));
        CITIES_BY_COUNTRY.put("Bosnia and Herzegovina", Arrays.asList("Sarajevo", "Banja Luka", "Tuzla", "Zenica", "Mostar"));
        CITIES_BY_COUNTRY.put("Montenegro", Arrays.asList("Podgorica", "Nikšić"));
        CITIES_BY_COUNTRY.put("Slovenia", Arrays.asList("Ljubljana", "Maribor"));
        CITIES_BY_COUNTRY.put("North Macedonia", Arrays.asList("Skopje", "Bitola", "Kumanovo"));
    }

    public static String getCountryForCity(String city) {
        if (city == null || city.isEmpty()) {
            return null;
        }
        for (Map.Entry<String, List<String>> entry : CITIES_BY_COUNTRY.entrySet()) {
            if (entry.getValue().contains(city)) {
                return entry.getKey();
            }
        }
        return null;
    }
}