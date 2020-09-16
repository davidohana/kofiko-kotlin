package kofiko

import org.amshove.kluent.shouldBeEmpty
import org.amshove.kluent.shouldBeEqualTo
import org.amshove.kluent.shouldContain
import org.amshove.kluent.shouldContainSame
import org.junit.Test
import java.awt.Color
import java.io.BufferedWriter
import java.io.FileWriter
import java.nio.file.Files
import java.nio.file.Paths
import kotlin.reflect.jvm.javaField

@Suppress("PropertyName")
class Config {
    var dummy = 1
}

class ParentOfNested {
    class Config {
        var dummy = 1
    }
}

object MyCar {
    var color = "red"
}


@Suppress("PropertyName")
class KofikoSampleConfig {
    var MyInt = 11
    var MyLong = 55L
    var MyString1 = "abc"
    var MyString2 = "def"
    var MyBool1 = false
    var MyBool2 = false
    var MyBool3 = false
    var MyDouble = 234.567
    var MyClass1 = FileWriter::class.java
    var MyClass2 = FileWriter::class.java
    var MyClass3: Class<out Appendable> = BufferedWriter::class.java
    var MyStrList = listOf("a", "b", "c")
    var MyStrList2 = listOf("a", "b", "c")
    var MyIntList = listOf(2, 4, 6)
    var MyLongList = listOf(2L, 4L, 6L)
    var MyFloatList = listOf(1.1f, 2.1f, 3.1f)
    var MyDoubleList = listOf(2.1, 3.1, 4.1)
    var MyBooleanList = listOf(true, false)
    var MyDict1 = mapOf("a" to 1, "b" to 2)
    var MyDict2 = mapOf("a" to 1, "b" to 2)
    var MyDict3 = mapOf("a" to 1, "b" to 2)
    var MyDict4 = mapOf("a" to 1, "b" to 2)
    var MyDict5 = mapOf("a" to 1, "b" to 2)
    var MyIntToFloatDict = mapOf(1 to 1.1, 2 to 2.2)

    companion object {
        val instance = KofikoSampleConfig()

        init {
            Kofiko.configure(instance)
        }
    }
}

@Suppress("unused", "PropertyName", "ProtectedInFinal")
class TestClass {
    var name = "name1"
    var Age = 11
    var COLOR = Color.BLUE
    private var priv = "private1"
    protected var prot = "protected1"
    internal var intern = "internal1"
    var _height = 123
    var NAME = "name2"
    var x = "x1"

    var privateSetter = 2
        private set

    @JvmField
    var jvmField = 2

    @JvmField
    protected var jvmFieldProtected = 22

    companion object {
        var staticField = 145
    }
}

class KofikoTest {

    @Test
    fun testGetFields() {
        val fields = getOverridableFields(TestClass::class.java)
        fields.size.shouldBeEqualTo(7)

        val fieldNames = fields.map { it.name }
        fieldNames.shouldContain("name")
        fieldNames.shouldContain("Age")
        fieldNames.shouldContain("COLOR")
        fieldNames.shouldContain("_height")
        fieldNames.shouldContain("NAME")
        fieldNames.shouldContain("jvmField")
        fieldNames.shouldContain("x")
    }

    @Test
    fun testGetCaseLookups1() {
        val settings = KofikoSettings()
        val lookups = getCaseLookups("myAge", settings.caseMapping)
        val expected = listOf("MyAge", "myage", "MYAGE", "myAge", "my_age", "MY_AGE")
        lookups.shouldContainSame(expected)
    }

    @Test
    fun testGetCaseLookups2() {
        val settings = KofikoSettings()
        val lookups = getCaseLookups("SNAKE_CASE", settings.caseMapping)
        val expected = listOf("SNAKE_CASE", "snake_case")
        lookups.shouldContainSame(expected)
    }

    @Test
    fun testGetSectionNameLookups() {
        val settings = KofikoSettings()
        val lookups = getSectionNameLookups("DatabaseConfig", settings)
        val expected = listOf(
            "DatabaseConfig", "databaseconfig", "DATABASECONFIG",
            "DATABASE_CONFIG", "database_config",
            "Database", "DATABASE", "database"
        )
        lookups.shouldContainSame(expected)
    }

    private fun getJson(): String {
        return """
    {
            "MyInt": 33,
            "MY_LONG": 66,
            "my_string2": "xxx",
            "MyBool2": true,
            "MyBool3": true,
            "MyDouble": 777.888,
            "MyClass2": "java.lang.Thread",
            "MyClass3": "java.io.FileWriter",
            "MyStrList": ["x","y","z"],
            "MyStrList2": ["^A|","xx"],
            "MyIntList": [1,3,5],
            "MyLongList": [1,3,5],
            "MyFloatList": [4.1,4.2,4.3],
            "MyDoubleList": [5.1,5.2,5.3],
            "MyBooleanList": [false, false, false, true],
            "MyDict1": {"a" : 10, "c": 30},
            "MyDict3": {"^C|": 0},
            "MyDict4": {"^C|": 0, "a" : 10, "c": 30},
            "MyIntToFloatDict": {"1":1.3, "2":2.3}
    }
    """
    }

    @Test
    fun testJsonFolderProvider() {
        val json = getJson()
        val path = Paths.get("test_cfg")
        Files.createDirectories(path)
        Files.writeString(path.resolve("kofiko_sample.json"), json)
        val provider = ConfigProviderJsonFolder(path)

        val settings = KofikoSettings()
        settings.configProviders.add(provider)
        settings.onOverride = PrintOverrideNotifier()
        val kofiko = Kofiko(settings)
        val cfg = KofikoSampleConfig()
        kofiko.configure(cfg)
        assertExpectedConfig(cfg)
    }

    @Test
    fun testJsonFileProvider() {
        var json = getJson()
        json = """ { "kofiko_sample": $json } """
        val provider = ConfigProviderJson.fromString(json)
        val settings = KofikoSettings()
        settings.configProviders.add(provider)
        settings.onOverride = PrintOverrideNotifier()
        val kofiko = Kofiko(settings)
        val cfg = KofikoSampleConfig()
        kofiko.configure(cfg)
        assertExpectedConfig(cfg)
    }


    @Test
    fun testEnvProvider() {
        val prefix = "test:kofiko_sample:"
        val env = mapOf(
            "${prefix}my_int" to "33",
            "${prefix}MyLong" to "66",
            "${prefix}MyString2" to "xxx",
            "${prefix}MyBool2" to "true",
            "${prefix}MyBool3" to "TRUE",
            "${prefix}MyDouble" to "777.888",
            "${prefix}MyClass2" to "java.lang.Thread",
            "${prefix}MyClass3" to "java.io.FileWriter",
            "${prefix}MyStrList" to "x,y,z",
            "${prefix}MyStrList2" to "^A|xx",
            "${prefix}MyIntList" to "1,3,5",
            "${prefix}MyLongList" to "1,3,5",
            "${prefix}MyFloatList" to "4.1,4.2,4.3",
            "${prefix}MyDoubleList" to "5.1,5.2,5.3",
            "${prefix}MyBooleanList" to "false,false,false,true",
            "${prefix}MyDict1" to "a:10,c:30",
            "${prefix}MyDict3" to "^C|",
            "${prefix}MyDict4" to "^C|a:10,c:30",
            "${prefix}MyIntToFloatDict" to "1:1.3,2:2.3",
        )

        val provider = ConfigProviderEnv("test", ":", env)
        val settings = KofikoSettings()
        settings.configProviders.add(provider)
        settings.onOverride = PrintOverrideNotifier()
        val kofiko = Kofiko(settings)
        val cfg = KofikoSampleConfig()
        kofiko.configure(cfg)
        assertExpectedConfig(cfg)
    }

    @Test
    fun testIniProvider() {
        val iniText = """
            [KOFIKO_SAMPLE]
            my_int=33
            MyLong=66
            MyString2=xxx
            MyBool2=true
            MyBool3=TRUE
            MyDouble=777.888
            MyClass2=java.lang.Thread
            MyClass3=java.io.FileWriter
            MyStrList=x,y,z
            MyStrList2=^A|xx
            MyIntList=1,3,5
            MyLongList=1,3,5
            MyFloatList=4.1,4.2,4.3
            MyDoubleList=5.1,5.2,5.3
            MyBooleanList=false,false,false,true
            MyDict1=a:10,c:30
            MyDict3=^C|
            MyDict4=^C|a:10,c:30
            MyIntToFloatDict=1:1.3,2:2.3
        """.trimIndent()

        val path = Paths.get("test_cfg")
        Files.createDirectories(path)
        val name = object {}.javaClass.enclosingMethod.name
        val iniPath = path.resolve(name)
        Files.writeString(iniPath, iniText)

        val provider = ConfigProviderIni(iniPath.toFile())
        val settings = KofikoSettings()
        settings.configProviders.add(provider)
        settings.onOverride = PrintOverrideNotifier()
        val kofiko = Kofiko(settings)
        val cfg = KofikoSampleConfig()
        kofiko.configure(cfg)
        assertExpectedConfig(cfg)
    }


    @Test
    fun testCliProvider() {
        val cliArgs = arrayOf(
            "-ov", "kofiko_sample_my_int=33",
            "-ov", "kofiko_sample_MyLong=66",
            "-ov", "kofiko_sample_MyString2=xxx",
            "-ov", "kofiko_sample_MyBool2=true",
            "-ov", "kofiko_sample_MyBool3=TRUE",
            "-ov", "kofiko_sample_MyDouble=777.888",
            "-ov", "kofiko_sample_MyClass2=java.lang.Thread",
            "-ov", "kofiko_sample_MyClass3=java.io.FileWriter",
            "-ov", "kofiko_sample_MyStrList=x,y,z",
            "-ov", "kofiko_sample_MyStrList2=^A|xx",
            "-ov", "kofiko_sample_MyIntList=1,3,5",
            "-ov", "kofiko_sample_MyLongList=1,3,5",
            "-ov", "kofiko_sample_MyFloatList=4.1,4.2,4.3",
            "-ov", "kofiko_sample_MyDoubleList=5.1,5.2,5.3",
            "-ov", "kofiko_sample_MyBooleanList=false,false,false,true",
            "-ov", "kofiko_sample_MyDict1=a:10,c:30",
            "-ov", "kofiko_sample_MyDict3=^C|",
            "-ov", "kofiko_sample_MyDict4=^C|a:10,c:30",
            "-ov", "kofiko_sample_MyDict5=a:1,b:2",
            "-ov", "kofiko_sample_MyIntToFloatDict=1:1.3,2:2.3",
            "-ov",
        )

        val provider = ConfigProviderCli(cliArgs)
        val settings = KofikoSettings()
        settings.configProviders.add(provider)
        settings.onOverride = PrintOverrideNotifier()
        val kofiko = Kofiko(settings)
        val cfg = KofikoSampleConfig()
        kofiko.configure(cfg)
        assertExpectedConfig(cfg)

    }


    private fun assertExpectedConfig(cfg: KofikoSampleConfig) {
        cfg.MyInt.shouldBeEqualTo(33)
        cfg.MyLong.shouldBeEqualTo(66L)
        cfg.MyString1.shouldBeEqualTo("abc")
        cfg.MyString2.shouldBeEqualTo("xxx")
        cfg.MyBool1.shouldBeEqualTo(false)
        cfg.MyBool2.shouldBeEqualTo(true)
        cfg.MyBool3.shouldBeEqualTo(true)
        cfg.MyDouble.shouldBeEqualTo(777.888)
        cfg.MyClass1.shouldBeEqualTo(FileWriter::class.java)
        cfg.MyClass2.shouldBeEqualTo(Thread::class.java)
        cfg.MyClass3.shouldBeEqualTo(FileWriter::class.java)
        cfg.MyStrList.shouldBeEqualTo(listOf("x", "y", "z"))
        cfg.MyStrList2.shouldBeEqualTo(listOf("a", "b", "c", "xx"))
        cfg.MyIntList.shouldBeEqualTo(listOf(1, 3, 5))
        cfg.MyIntList[0].javaClass.shouldBeEqualTo(Int::class.java)
        cfg.MyLongList.shouldBeEqualTo(listOf(1L, 3L, 5L))
        cfg.MyLongList[0].javaClass.shouldBeEqualTo(Long::class.java)
        cfg.MyFloatList.shouldBeEqualTo(listOf(4.1f, 4.2f, 4.3f))
        cfg.MyFloatList[0].javaClass.shouldBeEqualTo(Float::class.java)
        cfg.MyDoubleList.shouldBeEqualTo(listOf(5.1, 5.2, 5.3))
        cfg.MyDoubleList[0].javaClass.shouldBeEqualTo(Double::class.java)
        cfg.MyBooleanList.shouldBeEqualTo(listOf(false, false, false, true))
        cfg.MyDict1.shouldBeEqualTo(mapOf("a" to 10, "b" to 2, "c" to 30))
        cfg.MyDict2.shouldBeEqualTo(mapOf("a" to 1, "b" to 2))
        cfg.MyDict3.shouldBeEmpty()
        cfg.MyDict4.shouldBeEqualTo(mapOf("a" to 10, "c" to 30))
        cfg.MyDict5.shouldBeEqualTo(mapOf("a" to 1, "b" to 2))
        cfg.MyIntToFloatDict.shouldBeEqualTo(mapOf(1 to 1.3, 2 to 2.3))
    }

    @Test
    fun testProfiles() {
        class ProfiledConfig : ProfileSupport {
            var envName = "default"
            var port = 70

            override fun setProfile(profileName: String) {
                envName = profileName
            }
        }

        val env = mapOf(
            "ProfiledConfig_port" to "8080",
        )

        val provider = ConfigProviderEnv(env = env)
        val settings = KofikoSettings()
        settings.configProviders.add(provider)
        settings.onOverride = PrintOverrideNotifier()
        val kofiko = Kofiko(settings, "staging")
        val cfg = ProfiledConfig()
        kofiko.configure(cfg)

        cfg.port.shouldBeEqualTo(8080)
        cfg.envName.shouldBeEqualTo("staging")

        val overrides = kofiko.sectionNameToOverrides["ProfiledConfig"] ?: error("")
        overrides.shouldContain(
            FieldOverride(
                "ProfiledConfig",
                ProfiledConfig::port.javaField!!,
                70,
                8080,
                "ConfigProviderEnv"
            )
        )
        overrides.shouldContain(
            FieldOverride(
                "ProfiledConfig",
                ProfiledConfig::envName.javaField!!,
                "default",
                "staging",
                "Profile (staging)"
            )
        )
    }


    @Test
    fun testEmptySectionName() {
        val kofiko = Kofiko(KofikoSettings())
        kofiko.configure(Config())
        kofiko.sectionNameToOverrides.keys.shouldContain("Config")
    }

    @Test
    fun testNestedConfigSection() {
        class NestedInFun {
            var test = 1
        }

        val settings = KofikoSettings()

        val env = mapOf(
            "NestedInFun_test" to "2",
            "ParentOfNested.Config_dummy" to "3",
        )

        settings.configProviders.add(ConfigProviderEnv(env = env))
        settings.onOverride = PrintOverrideNotifier()
        val kofiko = Kofiko(settings)
        val cfg1 = ParentOfNested.Config()
        kofiko.configure(cfg1)
        kofiko.sectionNameToOverrides.keys.shouldContain("ParentOfNested.Config")
        cfg1.dummy.shouldBeEqualTo(3)

        val cfg2 = NestedInFun()
        kofiko.configure(cfg2)
        kofiko.sectionNameToOverrides.keys.shouldContain("NestedInFun")
        cfg2.test.shouldBeEqualTo(2)
    }

    @Test
    fun testAnnotatedConfigSection() {
        @ConfigSection("anno")
        class AnnotatedSection {
            var test = 1
        }

        val settings = KofikoSettings()
        settings.onOverride = PrintOverrideNotifier()
        val env = mapOf(
            "anno_test" to "2",
        )

        settings.configProviders.add(ConfigProviderEnv(env = env))
        val kofiko = Kofiko(settings)

        val cfg = AnnotatedSection()
        kofiko.configure(cfg)
        kofiko.sectionNameToOverrides.keys.shouldContain("anno")
        cfg.test.shouldBeEqualTo(2)
    }

    @Test
    fun testSecretOption() {
        class Config {
            @Secret
            var secret = "aaa"

            var notSecret = "bbb"

            @Secret
            var unchangedSecret = "unchanged"
        }

        val settings = KofikoSettings()
        settings.onOverride = PrintOverrideNotifier()

        val env = mapOf(
            "config_secret" to "secret",
            "config_not_secret" to "not a secret",
        )

        settings.configProviders.add(ConfigProviderEnv(env = env))
        val kofiko = Kofiko(settings)
        kofiko.configure(Config())
        kofiko.sectionNameToOverrides.keys.shouldContain("Config")
        kofiko.sectionNameToOverrides.size.shouldBeEqualTo(1)
        val overrides = kofiko.sectionNameToOverrides.values.first()
        overrides.size.shouldBeEqualTo(2)
        overrides[0].field.name.shouldBeEqualTo("secret")
        overrides[0].newValue.shouldBeEqualTo("[hidden]")
        overrides[1].field.name.shouldBeEqualTo("notSecret")
        overrides[1].newValue.shouldBeEqualTo("not a secret")
    }

    @Test
    fun testKotlinObject() {

        val settings = KofikoSettings()
        settings.onOverride = PrintOverrideNotifier()

        val env = mapOf(
            "my_car_color" to "blue",
        )

        settings.configProviders.add(ConfigProviderEnv(env = env))
        val kofiko = Kofiko(settings)
        kofiko.configure(MyCar)
        MyCar.color.shouldBeEqualTo("blue")
    }
}
