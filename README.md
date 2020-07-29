# Kangaroo ORM

Kangaroo is a Koltin-Postgres ORM built for those who search for a reliable and easy way to implement data storage with.

## Model

To define your model class, you should use the annotations as follows:

```kotlin
@Table("tableName")
class ModelExample(
    @Property("property1", "type") var property1 : T,
    @Property("property2", "type") var property2 : T,
    @Property("property3", "type") var property3 : T
)
```

## Property Values

- name : String

This is the column's name, and it should match the property name declared in the class.

- type : String

This is the type of the new column according to postgres type declaration.

- autoIncrement : Boolean

Sets if the column's value will be auto incremented. It's default value is false. 

- primaryKey : Boolean

Sets if the column's value will be primary key. It's default value is false. 

- nullable : Boolean

Sets if the column's value can be null. It's default value is false. 

- unique : Boolean

Sets if the column's value will be unique. It's default value is false.

- size : Int

Sets the column's size. Numeric types should not have sizes. It's default value is -1