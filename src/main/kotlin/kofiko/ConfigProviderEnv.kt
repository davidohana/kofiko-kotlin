package kofiko

class ConfigProviderEnv(
    prefix: String = "",
    sectionToOptionSeparator: String = "_",
    env: Map<String, String> = System.getenv()!!
) : ConfigProviderMap(prefix, sectionToOptionSeparator, env)
