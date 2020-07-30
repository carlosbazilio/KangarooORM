package database

import kotlin.reflect.*
import kotlin.reflect.full.*

/**
 * This class is the one how is going to create and manipulate the database
 */
class DatabaseManager {

    /** List with the properties annotations declared in the entity class */
    private val propertiesList = ArrayList<Property>()
    /** Table name declared in the entity class */
    private lateinit var tableName : String
    /** Entity class */
    private lateinit var cls : KClass<*>

    /** Postgres' numeric types */
    private val numericTypes = arrayListOf("int", "float", "double", "long", "short")

    /**
     * Method that sets the entity class and create its table according to the properties
     * @param c
     */
    fun <T : Any> setEntity(c : KClass<T>) {
        // setting the entity class and table name
        this.cls = c::class

        val tableName = c.annotations.find { it is Table } as Table
        this.tableName = tableName.tableName

        // setting the properties
        c.memberProperties.forEach {

            val property = it.annotations.find { annotation -> annotation is Property }
            if (property != null)
                propertiesList.add(property as Property)
        }

        // creating the table
        createTable()
    }

    /**
     * Method that selects the values from the database.
     * @param where
     * @return unit
     */
    fun select(where : String? = null) {
        // initiates the query with the select statement
        var sqlQuery = "SELECT "

        // for each property, puts its name in the select's fields.
        propertiesList.forEach {
            sqlQuery += it.name
            sqlQuery += if (propertiesList.indexOf(it) == propertiesList.size - 1)
                " "
            else
                ", "
        }

        // appends the from statement
        sqlQuery += "FROM $tableName"

        // appends the where statement
        if (where != null) {
            sqlQuery += " $where"
        }

        println(sqlQuery)
    }

    /**
     * Method that inserts an entity in the database
     * @param entity
     */
    fun <T : Any> insert(entity : T) {
        // gets the entity's declared properties
        val members = entity::class.declaredMemberProperties

        // initiates the query with the insert statement
        var sqlQuery = "INSERT INTO $tableName ("
        var index = 0

        /** properties names **/
        members.forEach {
            if (it.name == propertiesList[index].name) {
                sqlQuery += it.name

                sqlQuery += if (members.indexOf(it) == members.size - 1)
                    ") VALUES \n("
                else
                    ", "

            }
            index++
        }

        /** properties values **/
        index = 0
        members.forEach {
            if (it.name == propertiesList[index].name) {
                val prop = it as KMutableProperty1<T, *>
                val value = prop.get(entity)

                sqlQuery += checkNumericTypes(propertiesList[index].type, value.toString())

                sqlQuery += if (members.indexOf(it) == members.size - 1)
                    ");\n"
                else
                    ", "
            }
            index++
        }

        println(sqlQuery)
    }

    fun <T : Any> delete(entity : T) {
        var sqlQuery = "DELETE FROM $tableName WHERE "

        val members = entity::class.declaredMemberProperties
        var index = 0
        members.forEach {
            val property = propertiesList[index]

            if (it.name == property.name) {
                val prop = it as KMutableProperty1<T, *>
                val value = prop.get(entity)

                sqlQuery += "${it.name} = "
                sqlQuery += checkNumericTypes(property.type, value.toString())

                if (members.indexOf(it) != members.size -1) {
                    sqlQuery+= " AND "
                }
            }
            index++
        }
        sqlQuery += ";"

        println(sqlQuery)
    }

    fun <T : Any> update(entity : T) {
        var sqlQuery = "UPDATE $tableName SET "

        val members = entity::class.declaredMemberProperties
        var index = 0

        members.forEach {
            val property = propertiesList[index]

            if (it.name == property.name) {
                val prop = it as KMutableProperty1<T, *>
                val value = prop.get(entity)

                sqlQuery += "${it.name} = "
                sqlQuery += checkNumericTypes(property.type, value.toString())

                if (members.indexOf(it) != members.size -1) {
                    sqlQuery+= ", "
                }
            }
            index++
        }

        index = 0
        members.forEach {
            val prop = it as KMutableProperty1<T, *>
            val value = prop.get(entity)

            val property = propertiesList[index]
            if (containsPrimaryKey() != null && it.name == property.name && property.primaryKey)
                sqlQuery += " WHERE ${it.name} = $value"
        }
        sqlQuery += ";"

        println(sqlQuery)
    }

    private fun checkNumericTypes(type : String, value : String) : String {
        return if (type in numericTypes)
            value
        else
            "'$value'"
    }

    private fun containsPrimaryKey() : Property? {
        propertiesList.forEach {
            if (it.primaryKey)
                return it
        }
        return null
    }

    private fun createTable() {
        var sqlQuery = "DROP TABLE IF EXISTS $tableName;\n"
        var sequenceQuery = ""

        sqlQuery += "CREATE TABLE IF NOT EXISTS $tableName (\n"
        propertiesList.forEach {
            sqlQuery += "${it.name} ${it.type}"

            if (it.type !in numericTypes) {
                sqlQuery += "(${it.size})"
            }

            if (it.primaryKey) {
                sqlQuery += " primary key"
            }

            if (!it.nullable) {
                sqlQuery += " not null"
            }

            if (it.unique) {
                sqlQuery += " unique"
            }

            sqlQuery += if (propertiesList.indexOf(it) == propertiesList.size - 1)
                "\n);"
            else
                ",\n"

            if (it.autoIncrement) {
                val sequenceName = tableName + "_seq"

                sequenceQuery += "CREATE SEQUENCE $sequenceName INCREMENT 1 MINVALUE 1 START 1;\n"
                sequenceQuery += "ALTER TABLE $tableName ALTER COLUMN ${it.name} SET DEFAULT nextval('$sequenceName');\n"
            }
        }

        println(sqlQuery)
        println(sequenceQuery)
    }
}