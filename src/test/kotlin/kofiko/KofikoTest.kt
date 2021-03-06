package kofiko

import org.amshove.kluent.shouldBeEmpty
import org.amshove.kluent.shouldBeEqualTo
import org.amshove.kluent.shouldContain
import org.amshove.kluent.shouldContainSame
import org.junit.Test
import java.awt.Color
import java.io.BufferedWriter
import java.io.File
import java.io.FileWriter
import java.math.BigInteger
import java.nio.file.AccessMode
import java.nio.file.Files
import java.nio.file.Paths
import java.text.SimpleDateFormat
import java.util.*
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
    var year = 2020
}

enum class Colors { Red, Green, Blue }

@Suppress("PropertyName")
class KofikoSampleConfig {
    val MyInt = 11
    val MyLong: Long? = 55L
    val MyString1 = "abc"
    val MyString2: String? = "def"
    var MyBool1 = false
    var MyBool2 = false
    var MyBool3 = false

    @ConfigName(name = "Dbl")
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
    val MyDict1 = mapOf("a" to 1, "b" to 2)
    var MyDict2 = mapOf("a" to 1, "b" to 2)
    var MyDict3 = mapOf("a" to 1, "b" to 2)
    var MyDict4 = mapOf("a" to 1, "b" to 2)
    var MyDict5 = mapOf("a" to 1, "b" to 2)
    val MyIntToFloatDict = mapOf(1 to 1.1, 2 to 2.2)
    var MyColor = Colors.Red
    val MyAccessMode = AccessMode.EXECUTE

    companion object {
        val instance = KofikoSampleConfig()

        init {
            Kofiko.add(instance)
        }
    }
}

@Suppress("unused", "PropertyName", "ProtectedInFinal")
class AccessModesSample {
    var privateSetter = 2
        private set

    val valProperty = "r"

    var name = "name1"
    var Age = 11
    var COLOR = Color.BLUE
    private var priv = "private1"
    protected var prot = "protected1"
    internal var intern = "internal1"
    var _height = 123
    var NAME = "name2"
    var x = "x1"

    @JvmField
    var jvmFieldVar = 2

    @JvmField
    val jvmFieldVal = 27

    @JvmField
    protected var jvmFieldProtectedVar = 22

    @JvmField
    internal var jvmFieldInternalVar = 224

    companion object {
        var staticField = 145
    }
}

class KofikoTest {

    @Test
    fun testGetFields() {
        val fields = getOverridableFields(AccessModesSample::class.java, false)

        val fieldNames = fields.map { it.name }
        val expected = mutableListOf(
            "name", "Age", "COLOR", "_height",
            "NAME", "jvmFieldVar", "x", "jvmFieldInternalVar"
        )
        fieldNames.shouldContainSame(expected)

        val fieldsWithReadOnly = getOverridableFields(AccessModesSample::class.java, true)

        val fieldNames2 = fieldsWithReadOnly.map { it.name }
        expected.add("valProperty")
        expected.add("privateSetter")
        expected.add("jvmFieldVal")

        fieldNames2.shouldContainSame(expected)
    }

    @Test
    fun testGetCaseLookups1() {
        val lookupProvider = DefaultNameLookupProvider()
        val lookups = lookupProvider.getCaseLookups("myAge")
        val expected = listOf("MyAge", "myage", "MYAGE", "myAge", "my_age", "MY_AGE", "my-age", "MY-AGE")
        lookups.shouldContainSame(expected)
    }

    @Test
    fun testGetCaseLookups2() {
        val lookupProvider = DefaultNameLookupProvider()
        val lookups = lookupProvider.getCaseLookups("SNAKE_CASE")
        val expected = listOf("SNAKE_CASE", "snake_case")
        lookups.shouldContainSame(expected)
    }

    @Test
    fun testGetSectionNameLookups() {
        val lookupProvider = DefaultNameLookupProvider()
        val lookups = lookupProvider.getSectionLookups("DatabaseConfig")
        val expected = listOf(
            "DatabaseConfig", "databaseconfig", "DATABASECONFIG",
            "DATABASE_CONFIG", "database_config",
            "Database", "DATABASE", "database", "database-config", "DATABASE-CONFIG"
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
            "MyBool3": 1,
            "Dbl": 777.888,
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
            "MyIntToFloatDict": {"1":1.3, "2":2.3},
            "MyColor": "Green",
            "MyAccessMode": "READ"
    }
    """
    }

    fun writeTextFile(name: String, content: String): File {
        val path = Paths.get("test_cfg")
        Files.createDirectories(path)
        Files.writeString(path.resolve(name), content)
        return path.resolve(name).toFile()
    }

    @Test
    fun testJsonFolderProvider() {
        val json = getJson()
        writeTextFile("kofiko_sample.json", json)
        val provider = JsonFolderConfigProvider("test_cfg")

        val settings = KofikoSettings()
        settings.configProviders.add(provider)
        settings.onOverride = PrintOverrideNotifier()
        val kofiko = Kofiko(settings)
        val cfg = KofikoSampleConfig()
        kofiko.add(cfg)
        assertExpectedConfig(cfg)
    }

    @Test
    fun testJsonProvider() {
        var json = getJson()
        json = """ { "kofiko_sample": $json } """
        val settings = KofikoSettings()
        settings.configProviders.add(JsonConfigProvider(ConfigSource.fromText(json)))
        settings.onOverride = PrintOverrideNotifier()
        val kofiko = Kofiko(settings)
        val cfg = KofikoSampleConfig()
        kofiko.add(cfg)
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
            "${prefix}MyBool3" to "1",
            "${prefix}Dbl" to "777.888",
            "${prefix}MyClass2" to "java.lang.Thread",
            "${prefix}MyClass3" to "java.io.FileWriter",
            "${prefix}MyStrList" to "x,y,z",
            "${prefix}MyStrList2" to "^A|xx",
            "${prefix}MyIntList" to "1,3,5",
            "${prefix}MyLongList" to "1,3,5",
            "${prefix}MyFloatList" to "4.1,4.2,4.3",
            "${prefix}MyDoubleList" to "5.1,5.2,5.3",
            "${prefix}MyBooleanList" to "FALSE,f,no,y",
            "${prefix}MyDict1" to "a:10,c:30",
            "${prefix}MyDict3" to "^C|",
            "${prefix}MyDict4" to "^C|a:10,c:30",
            "${prefix}MyIntToFloatDict" to "1:1.3,2:2.3",
            "${prefix}MyColor" to "Green",
            "${prefix}MyAccessMode" to "READ",
        )

        val provider = EnvConfigProvider("test", ":", env)
        val settings = KofikoSettings()
        settings.configProviders.add(provider)
        settings.onOverride = PrintOverrideNotifier()
        val kofiko = Kofiko(settings)
        val cfg = KofikoSampleConfig()
        kofiko.add(cfg)
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
            MyBool3=   TRUE
            Dbl=    777.888
            MyClass2    =java.lang.Thread
            MyClass3    =   java.io.FileWriter
                MyStrList=x,y,z
            MyStrList2=^A|xx
            MyIntList=1,3,5
            MyLongList=[1, 3, 5]
            MyFloatList=4.1,4.2,4.3
            MyDoubleList=5.1,5.2,5.3
            MyBooleanList=0,off,n,YES
            MyDict1={"a":10, "c":30}
            MyDict3=^C|
            MyDict4=^C|a:10,c:30
            MyIntToFloatDict={"1":1.3,"2":2.3}
            MyColor=Green
            #comment            
            MyAccessMode=READ
        """.trimIndent()

        val settings = KofikoSettings(IniConfigProvider(ConfigSource.fromText(iniText)))
        settings.onOverride = PrintOverrideNotifier()
        val kofiko = Kofiko(settings)
        val cfg = KofikoSampleConfig()
        kofiko.add(cfg)
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
            "-ov", "kofiko_sample_Dbl=777.888",
            "-ov", "kofiko_sample_MyClass2=java.lang.Thread",
            "-ov", "kofiko_sample_MyClass3=java.io.FileWriter",
            "-ov", "kofiko_sample_MyStrList=x,y,z",
            "-ov", "kofiko_sample_MyStrList2=^A|xx",
            "-ov", "kofiko_sample_MyIntList=1,3,5",
            "-ov", "kofiko_sample_MyLongList=1,3,5",
            "-ov", "kofiko_sample_MyFloatList=4.1,4.2,4.3",
            "-ov", "kofiko_sample_MyDoubleList=5.1,5.2,5.3",
            "-ov", "kofiko_sample_MyBooleanList=f,0,0,t",
            "-ov", "kofiko_sample_MyDict1=a:10,c:30",
            "-ov", "kofiko_sample_MyDict3=^C|",
            "-ov", "kofiko_sample_MyDict4=^C|a:10,c:30",
            "-ov", "kofiko_sample_MyDict5=a:1,b:2",
            "-ov", "kofiko_sample_MyIntToFloatDict=1:1.3,2:2.3",
            "-ov", "kofiko_sample_MyColor=Green",
            "-ov", "kofiko_sample_MyAccessMode=READ",
            "-ov",
        )

        val provider = CliConfigProvider(cliArgs)
        val settings = KofikoSettings()
        settings.configProviders.add(provider)
        settings.onOverride = PrintOverrideNotifier()
        val kofiko = Kofiko(settings)
        val cfg = KofikoSampleConfig()
        kofiko.add(cfg)
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
        cfg.MyColor.shouldBeEqualTo(Colors.Green)
        cfg.MyAccessMode.shouldBeEqualTo(AccessMode.READ)
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

        val provider = EnvConfigProvider(env = env)
        val settings = KofikoSettings()
        settings.configProviders.add(provider)
        settings.onOverride = PrintOverrideNotifier()
        val kofiko = Kofiko(settings, "staging")
        val cfg = ProfiledConfig()
        kofiko.add(cfg)

        cfg.port.shouldBeEqualTo(8080)
        cfg.envName.shouldBeEqualTo("staging")

        val overrides = kofiko.sectionNameToOverrides["ProfiledConfig"] ?: error("")
        overrides.shouldContain(
            FieldOverride(
                ProfiledConfig::port.javaField!!,
                "ProfiledConfig",
                "port",
                70,
                8080,
                EnvConfigProvider::class.java.simpleName
            )
        )
        overrides.shouldContain(
            FieldOverride(
                ProfiledConfig::envName.javaField!!,
                "ProfiledConfig",
                "envName",
                "default",
                "staging",
                "Profile (staging)"
            )
        )
    }


    @Test
    fun testEmptySectionName() {
        val kofiko = Kofiko(KofikoSettings())
        kofiko.add(Config())
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

        settings.configProviders.add(EnvConfigProvider(env = env))
        settings.onOverride = PrintOverrideNotifier()
        val kofiko = Kofiko(settings)
        val cfg1 = ParentOfNested.Config()
        kofiko.add(cfg1)
        kofiko.sectionNameToOverrides.keys.shouldContain("ParentOfNested.Config")
        cfg1.dummy.shouldBeEqualTo(3)

        val cfg2 = NestedInFun()
        kofiko.add(cfg2)
        kofiko.sectionNameToOverrides.keys.shouldContain("NestedInFun")
        cfg2.test.shouldBeEqualTo(2)
    }

    @Test
    fun testAnnotatedConfigSection() {
        @ConfigName("anno")
        class AnnotatedSection {
            var test = 1
        }

        val settings = KofikoSettings()
        settings.onOverride = PrintOverrideNotifier()
        val env = mapOf(
            "anno_test" to "2",
        )

        settings.configProviders.add(EnvConfigProvider(env = env))
        val kofiko = Kofiko(settings)

        val cfg = AnnotatedSection()
        kofiko.add(cfg)
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

        settings.configProviders.add(EnvConfigProvider(env = env))
        val kofiko = Kofiko(settings)
        kofiko.add(Config())
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
            "my_car_year" to "2019",
        )

        settings.configProviders.add(EnvConfigProvider(env = env))
        val kofiko = Kofiko(settings)
        kofiko.add(MyCar)
        MyCar.color.shouldBeEqualTo("blue")
        MyCar.year.shouldBeEqualTo(2019)
    }

    @Test
    fun testDataClass() {

        data class TheData(var dummy: Int = 1)


        val settings = KofikoSettings()
        settings.onOverride = PrintOverrideNotifier()

        val env = mapOf(
            "the_data_dummy" to "3",
        )

        settings.configProviders.add(EnvConfigProvider(env = env))
        val kofiko = Kofiko(settings)
        val cfg = TheData()
        kofiko.add(cfg)
        cfg.dummy.shouldBeEqualTo(3)
    }


    @Test
    fun testPropertiesProvider() {
        val content = """
            # comment
            kofiko-sample.my_int=33
            kofiko-sample.MyLong=66
            kofiko-sample.MyString2 = xxx
             kofiko-sample.MyBool2=true
                kofiko-sample.MyBool3=TRUE
            kofiko-sample.Dbl=777.888
            kofiko-sample.MyClass2=     java.lang.Thread
            kofiko-sample.MyClass3=java.io.FileWriter
            kofiko-sample.MyStrList     =   x,y,z

            kofiko-sample.MyStrList2=^A|xx
            kofiko-sample.MyIntList=1,3,5
            kofiko-sample.MyLongList=1,3,5
            kofiko-sample.MyFloatList=4.1,4.2,4.3
            kofiko-sample.MyDoubleList=5.1,5.2,5.3
            kofiko-sample.MyBooleanList=false,false,false,true
            kofiko-sample.MyDict1=a:10,c:30
            kofiko-sample.MyDict3=^C|
            kofiko-sample.MyDict4=^C|a:10,c:30
            kofiko-sample.MyIntToFloatDict=1:1.3,2:2.3
            kofiko-sample.MyColor=Green
            kofiko-sample.MyAccessMode=READ
            """.trimIndent()

        val properties = content.toProperties()
        val provider = PropertiesConfigProvider(properties)
        val settings = KofikoSettings()
        settings.configProviders.add(provider)
        settings.onOverride = PrintOverrideNotifier()
        val kofiko = Kofiko(settings)
        val cfg = KofikoSampleConfig()
        kofiko.add(cfg)
        assertExpectedConfig(cfg)
    }

    @Test
    fun testEnvFileProvider() {
        val content = """
            # comment
            "kofiko-sample_my_int"="33"
            'kofiko-sample_MyLong'='66'
            kofiko-sample_MyString2 = xxx
              kofiko-sample_MyBool2=true
                kofiko-sample_MyBool3=TRUE
            kofiko-sample_Dbl=777.888
            kofiko-sample_MyClass2=     java.lang.Thread
            kofiko-sample_MyClass3=java.io.FileWriter
            kofiko-sample_MyStrList     =   x,y,z
            kofiko-sample_MyStrList2=^A|xx
            kofiko-sample_MyIntList=1,3,5
            kofiko-sample_MyLongList=1,3,5
            kofiko-sample_MyFloatList=4.1,4.2,4.3
            kofiko-sample_MyDoubleList=5.1,5.2,5.3
            kofiko-sample_MyBooleanList=false,false,false,true
            kofiko-sample_MyDict1=a:10,c:30
            kofiko-sample_MyDict3=^C|
            kofiko-sample_MyDict4=^C|a:10,c:30
            kofiko-sample_MyIntToFloatDict=1:1.3,2:2.3
            kofiko-sample_MyColor=Green
            kofiko-sample_MyAccessMode=READ
            """.trimIndent()

        val provider = EnvFileConfigProvider(ConfigSource.fromText(content))
        val settings = KofikoSettings()
        settings.configProviders.add(provider)
        settings.onOverride = PrintOverrideNotifier()
        val kofiko = Kofiko(settings)
        val cfg = KofikoSampleConfig()
        kofiko.add(cfg)
        assertExpectedConfig(cfg)
    }

    @Test
    fun testParseDate() {
        class TestSection {
            var myDate = Date(0)
        }

        val map = mapOf("test_section_my_date" to "2020-09-18 17:24:44")

        val settings = KofikoSettings(MapConfigProvider(map))
        settings.objectMapper.dateFormat = SimpleDateFormat("yyyy-MM-dd hh:mm:ss")
        settings.onOverride = PrintOverrideNotifier()
        val kofiko = Kofiko(settings)
        val cfg = TestSection()
        kofiko.add(cfg)
        val expDate = GregorianCalendar(2020, Calendar.SEPTEMBER, 18, 17, 24, 44).time
        cfg.myDate.shouldBeEqualTo(expDate)
    }

    @Test
    fun testParseComplexObject() {
        class Person {
            var name: String = ""
            var age: Int = 0
        }

        class TestSection {
            var me = Person()
        }

        val map = mapOf("test_section_me" to """ { "name": "Dave", "age": 41 } """)

        val settings = KofikoSettings(MapConfigProvider(map))
        settings.onOverride = PrintOverrideNotifier()
        val kofiko = Kofiko(settings)
        val cfg = TestSection()
        kofiko.add(cfg)
        cfg.me.name.shouldBeEqualTo("Dave")
        cfg.me.age.shouldBeEqualTo(41)
    }

    @Test
    fun testSectionInheritance() {
        open class Food {
            var calories = 200
        }

        class Pizza : Food() {
            var extras = "pine"
        }

        val map = mapOf("pizza_calories" to "300", "pizza_extras" to "olives")

        val settings = KofikoSettings(MapConfigProvider(map))
        settings.onOverride = PrintOverrideNotifier()
        val kofiko = Kofiko(settings)
        val cfg = Pizza()
        kofiko.add(cfg)
        cfg.extras.shouldBeEqualTo("olives")
        cfg.calories.shouldBeEqualTo(300)

    }

    @Test
    fun testParseSet() {
        class TestSection {
            var MySet = setOf("x", "y", "z")
            var MySet2 = setOf("x", "y", "z")
        }

        val map = mapOf(
            "test_section_my_set" to "a,b,x,x",
            "test_section_my_set2" to "^A|a,b,x,x"
        )

        val settings = KofikoSettings(MapConfigProvider(map))
        settings.onOverride = PrintOverrideNotifier()
        val kofiko = Kofiko(settings)
        val cfg = TestSection()
        kofiko.add(cfg)
        cfg.MySet.shouldBeEqualTo(setOf("x", "b", "a"))
        cfg.MySet2.shouldBeEqualTo(setOf("x", "b", "a", "y", "z"))
    }

    @Test
    fun testParseBigInteger() {
        class TestSection {
            var num = BigInteger.ONE
        }

        val map = mapOf("test_section_num" to "2")

        val settings = KofikoSettings(MapConfigProvider(map))
        settings.onOverride = PrintOverrideNotifier()
        val kofiko = Kofiko(settings)
        val cfg = TestSection()
        kofiko.add(cfg)
        cfg.num.shouldBeEqualTo(BigInteger.TWO)
    }

    @Test
    fun testFilesProvider() {
        class TestSection {
            var num = 1
            var text = "ttt"
        }

        val json = """{ "test" : { "num": 2 } }"""
        val ini = """
            [TEST]
            text=aaa
        """.trimIndent()

        val name = object {}.javaClass.enclosingMethod.name
        val jsonFile = writeTextFile("$name.JSON", json)
        val iniFile = writeTextFile("$name.ini", ini)

        val settings = KofikoSettings()
            .addFiles(File("1.json"), jsonFile, iniFile)
        settings.onOverride = PrintOverrideNotifier()
        val kofiko = Kofiko(settings)
        val cfg = TestSection()
        kofiko.add(cfg)
        cfg.num.shouldBeEqualTo(2)
        cfg.text.shouldBeEqualTo("aaa")
    }

    @Test
    fun testParseQuotedString() {
        class Test {
            var str1 = "david"
            var str2 = "david"
            var str3 = "david"
        }

        val map = mapOf("test_str1" to "dave", "test_str2" to """ x="y" """, "test_str3" to "'dummy'")

        val settings = KofikoSettings(MapConfigProvider(map))
        settings.onOverride = PrintOverrideNotifier()
        val kofiko = Kofiko(settings)
        val cfg = Test()
        kofiko.add(cfg)
        cfg.str1.shouldBeEqualTo("dave")
        cfg.str2.shouldBeEqualTo(""" x="y" """)
        cfg.str3.shouldBeEqualTo("'dummy'")
    }
}
