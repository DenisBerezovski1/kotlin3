/*
KOTLIN - HW3
------------
Продолжаем дорабатывать домашнее задание из предыдущего семинара.
За основу берём код решения из предыдущего домашнего задания.

— Измените класс Person так, чтобы он содержал список телефонов и список почтовых адресов, связанных с человеком.
— Теперь в телефонной книге могут храниться записи о нескольких людях.
  Используйте для этого наиболее подходящую структуру данных.
— Команда AddPhone теперь должна добавлять новый телефон к записи соответствующего человека.
— Команда AddEmail теперь должна добавлять новый email к записи соответствующего человека.
— Команда show должна принимать в качестве аргумента имя человека и выводить связанные с ним телефоны
  и адреса электронной почты.
— Добавьте команду find, которая принимает email или телефон и выводит список людей,
  для которых записано такое значение.
*/

import com.diogonunes.jcolor.Ansi
import com.diogonunes.jcolor.Attribute
import java.util.*
import kotlin.system.exitProcess

val HELP_MESSAGE: String = """
        Перечень команд:
        
              exit
                - прекращение работы
                
              help
                - справка
                
              add <Имя> phone <Номер телефона>
                - сохранение записи с введенными именем и номером телефона
                - добавление нового номера телефона к уже имеющейся записи соответствующего человека
                 
              add <Имя> email <Адрес электронной почты>
                - сохранение записи с введенными именем и адрес электронной почты
                - добавление нового адреса электронной почты к уже имеющейся записи соответствующего человека
                
              show <Имя>
                - выводит по введенному имени человека связанные с ним телефоны и адреса электронной почты
                
              find <критерий>
                - выводит по введенному критерию (номер телефона или адрес электронной почты) список людей
              
    """.trimIndent()
val COMMON_ERROR_MESSAGE: String =
    Ansi.colorize("Ошибка! Команда введена неверно. Список команд ниже", Attribute.BRIGHT_RED_TEXT())

var phoneBook = mutableMapOf<String, Person>()


sealed interface Command {
    fun execute()
    fun isValid(): Boolean
}

data object ExitCommand : Command {

    override fun execute() {
        exitProcess(0)
    }

    override fun isValid(): Boolean {
        return true
    }

    override fun toString(): String {
        return Ansi.colorize("Введена команда \"exit\"", Attribute.BRIGHT_GREEN_TEXT())
    }
}

data object HelpCommand : Command {

    override fun execute() {
        println(HELP_MESSAGE)
    }

    override fun isValid(): Boolean {
        return true
    }

    override fun toString(): String {
        return Ansi.colorize("Вывод справочной информации", Attribute.BRIGHT_GREEN_TEXT())
    }
}

class AddUserPhoneCommand(private val entryData: List<String>) : Command {

    private val phonePattern = Regex("[+]+\\d+")
    private val entryPhone = entryData[entryData.indexOf("phone") + 1]

    override fun execute() {
        if (phoneBook.containsKey(entryData[0])) {
            phoneBook[entryData[0]]?.contacts?.get("phone")?.add(entryPhone)
        } else {
            val person = Person(
                entryData[0],
                contacts = mutableMapOf(Pair("phone", mutableListOf(entryPhone)), Pair("email", mutableListOf()))
            )
            phoneBook[entryData[0]] = person
        }
    }

    override fun isValid(): Boolean {
        return entryPhone.matches(phonePattern) && entryData.size <= 3
    }

    override fun toString(): String {
        return Ansi.colorize(
            "Введена команда записи нового пользователя ${entryData[0]} с номером телефона $entryPhone",
            Attribute.BRIGHT_GREEN_TEXT()
        )
    }
}

class AddUserEmailCommand(private val entryData: List<String>) : Command {

    private val emailPattern = Regex("[a-zA-z0-9]+@[a-zA-z0-9]+[.]([a-zA-z0-9]{2,4})")
    private val entryEmail = entryData[entryData.indexOf("email") + 1]

    override fun execute() {
        if (phoneBook.containsKey(entryData[0])) {
            phoneBook[entryData[0]]?.contacts?.get("email")?.add(entryEmail)
        } else {
            val person = Person(
                entryData[0],
                contacts = mutableMapOf(Pair("phone", mutableListOf()), Pair("email", mutableListOf(entryEmail)))
            )
            phoneBook[entryData[0]] = person
        }
    }

    override fun isValid(): Boolean {
        return entryEmail.matches(emailPattern) && entryData.size <= 3
    }

    override fun toString(): String {
        return Ansi.colorize(
            "Введена команда записи нового пользователя ${entryData[0]} с адресом электронной почты $entryEmail",
            Attribute.BRIGHT_GREEN_TEXT()
        )
    }
}

class ShowCommand(private val name: String) : Command {
    override fun execute() {
        if (phoneBook.isEmpty()) {
            println("Phonebook is not initialized")
        } else if (phoneBook.containsKey(name)) {
            println(phoneBook[name])
        } else {
            println("Person with name $name was not found")
        }
    }

    override fun isValid(): Boolean {
        return true
    }

    override fun toString(): String {
        return Ansi.colorize("Введена команда \"show\"", Attribute.BRIGHT_GREEN_TEXT())
    }

}

data class Person(
    var name: String,
    var contacts: MutableMap<String, MutableList<String>> = mutableMapOf(
        "phone" to mutableListOf(),
        "email" to mutableListOf()
    )
) {
    override fun toString(): String {
        return buildString {
            append("Пользователь: ")
            append(name.replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() })
            append(buildString {
                if (contacts["phone"]?.isNotEmpty() == true) {
                    append("\n\t")
                    append("phone(s): ")
                    append(
                        contacts["phone"].toString()
                            .replace("[", "")
                            .replace("]", "")
                    )
                }
            })
            append(buildString {
                if (contacts["email"]?.isNotEmpty() == true) {
                    append("\n\t")
                    append("email(s): ")
                    append(
                        contacts["email"].toString()
                            .replace("[", "")
                            .replace("]", "")
                    )
                }
            })
            append("\n")
        }
    }
}

class FindCommand(private val value: String) : Command {
    override fun execute() {
        val persons = mutableListOf<Person>()
        if (phoneBook.isEmpty()) {
            println("Phonebook is not initialized")
        } else {
            for (person in phoneBook.values) {
                if (person.contacts["phone"]!!.contains(value) or person.contacts["email"]!!.contains(value)) {
                    persons.add(person)
                }
            }
        }
        if (persons.isEmpty()) {
            println("Person with $value was not found")
        } else {
            persons.forEach { person ->
                println(person)
            }
        }
    }

    override fun isValid(): Boolean {
        return true
    }

    override fun toString(): String {
        return Ansi.colorize("Введена команда \"find\"", Attribute.BRIGHT_GREEN_TEXT())
    }

}


fun readCommand(): Command {
    print("> ")
    val entryData: List<String> = readln().lowercase().split(' ')

    return when (entryData[0]) {
        "add" -> {
            if (entryData.size > 3 && "phone" in entryData && "email" !in entryData) {
                AddUserPhoneCommand(entryData.subList(1, entryData.size))
            } else if (entryData.size > 3 && "phone" !in entryData && "email" in entryData) {
                AddUserEmailCommand(entryData.subList(1, entryData.size))
            } else {
                println(COMMON_ERROR_MESSAGE)
                HelpCommand
            }
        }

        "show" -> {
            if (entryData.size > 1) {
                ShowCommand(entryData[1])
            } else {
                println(COMMON_ERROR_MESSAGE)
                HelpCommand
            }
        }

        "find" -> {
            if (entryData.size > 1) {
                FindCommand(entryData[1])
            } else {
                println(COMMON_ERROR_MESSAGE)
                HelpCommand
            }
        }

        "help" -> HelpCommand
        "exit" -> ExitCommand
        else -> {
            println(COMMON_ERROR_MESSAGE)
            return HelpCommand
        }
    }
}


fun hw3() {

    println("Введите команду или \"help\" для вывода списка команд ")

    while (true) {
        val command: Command = readCommand()
        if (command.isValid()) {
            println(command)
            command.execute()
        } else {
            println(COMMON_ERROR_MESSAGE)
            println(HELP_MESSAGE)
        }
    }

}
