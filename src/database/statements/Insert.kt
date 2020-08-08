package database.statements

import database.DatabaseHelper.checkTypes
import database.DatabaseHelper.getMappedPropertyOrNull
import database.DatabaseExecutor
import database.DatabaseManager
import kotlin.reflect.KMutableProperty1
import kotlin.reflect.full.declaredMemberProperties

class Insert : IQuery
{
    override var sqlQuery: String = ""
    override lateinit var databaseManager: DatabaseManager

    override fun setDatabaseManager(databaseManager: DatabaseManager): Insert {
        this.databaseManager = databaseManager
        return this
    }

    /**
     * Method that inserts an entity in the database
     * @param entity
     */
    fun <T : Any> insert(entity : T) : Insert {
        // gets the entity's declared properties
        val members = entity::class.declaredMemberProperties

        // initiates the query with the insert statement
        sqlQuery = "INSERT INTO ${databaseManager.tableName} ("

        // puts the properties' names in the insert's fields
        members.forEach {
            // checks if the member is a mapped property
            val property = getMappedPropertyOrNull(it.name, databaseManager.propertiesList)

            if (property != null) {
                sqlQuery += "${it.name}, "
            }
        }

        // removes the last comma
        sqlQuery = sqlQuery.take(sqlQuery.length - 2)
        sqlQuery += ") VALUES \n("

        // puts the entity's declaredMember values in the insert's values
        members.forEach {
            val property = getMappedPropertyOrNull(it.name, databaseManager.propertiesList)

            // checks if the member is a mapped property
            if (property != null) {
                // converts the entity's declaredMember to a mutable property, so we can retrieve the values.
                val prop = it as KMutableProperty1<T, *>
                // gets the value
                val value = prop.get(entity)

                // checks if we have to wrap the value in ''
                sqlQuery += "${checkTypes(property.type, value.toString())}, "
            }
        }

        // removes the last comma
        sqlQuery = sqlQuery.take(sqlQuery.length - 2)
        sqlQuery += ");\n"

        return this
    }

    override fun execute() {
        DatabaseExecutor.executeOperation(sqlQuery, true)
    }
}