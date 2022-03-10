@file:Suppress("UNUSED_PARAMETER")

package lesson11.task1

import kotlin.math.pow

/**
 * Класс "полином с вещественными коэффициентами".
 *
 * Общая сложность задания -- средняя, общая ценность в баллах -- 16.
 * Объект класса -- полином от одной переменной (x) вида 7x^4+3x^3-6x^2+x-8.
 * Количество слагаемых неограничено.
 *
 * Полиномы можно складывать -- (x^2+3x+2) + (x^3-2x^2-x+4) = x^3-x^2+2x+6,
 * вычитать -- (x^3-2x^2-x+4) - (x^2+3x+2) = x^3-3x^2-4x+2,
 * умножать -- (x^2+3x+2) * (x^3-2x^2-x+4) = x^5+x^4-5x^3-3x^2+10x+8,
 * делить с остатком -- (x^3-2x^2-x+4) / (x^2+3x+2) = x-5, остаток 12x+16
 * вычислять значение при заданном x: при x=5 (x^2+3x+2) = 42.
 *
 * В конструктор полинома передаются его коэффициенты, начиная со старшего.
 * Нули в середине и в конце пропускаться не должны, например: x^3+2x+1 --> Polynom(1.0, 2.0, 0.0, 1.0)
 * Старшие коэффициенты, равные нулю, игнорировать, например Polynom(0.0, 0.0, 5.0, 3.0) соответствует 5x+3
 */

class Polynom(private vararg val coeffs: Double) {
    private val list = coeffs.toList()

    /**
     * Геттер: вернуть значение коэффициента при x^i
     */
    fun coeff(i: Int): Double = list.reversed()[i]


    /**
     * Расчёт значения при заданном x
     */
    fun getValue(x: Double): Double {
        var res = 0.0
        for (i in 0..list.lastIndex) {
            res += list.reversed()[i] * x.pow(i)
        }
        return res
    }

    /**
     * Степень (максимальная степень x при ненулевом слагаемом, например 2 для x^2+x+1).
     *
     * Степень полинома с нулевыми коэффициентами считать равной 0.
     * Слагаемые с нулевыми коэффициентами игнорировать, т.е.
     * степень 0x^2+0x+2 также равна 0.
     */
    fun degree(): Int {
        var res = 0
        for (i in 0..list.lastIndex) {
            if (list.reversed()[i] != 0.0) res = i
        }
        return res
    }

    /**
     * Сложение
     */
    operator fun plus(other: Polynom): Polynom {
        val res = mutableListOf<Double>()
        val min = minOf(other.degree(), list.lastIndex)
        for (i in 0..min) {
            res += other.coeff(i) + list.reversed()[i]
        }
        if (min == list.lastIndex) {
            for (i in list.lastIndex + 1..other.degree())
                res += other.coeff(i)
        } else {
            for (i in other.degree() + 1..list.lastIndex)
                res += list.reversed()[i]

        }
        return Polynom(*res.reversed().toDoubleArray())

    }

    /**
     * Смена знака (при всех слагаемых)
     */
    operator fun unaryMinus(): Polynom = Polynom(*coeffs.map { -it }.toDoubleArray())

    /**
     * Вычитание
     */
    operator fun minus(other: Polynom): Polynom = this + (-other)


    /**
     * Умножение
     */
    operator fun times(other: Polynom): Polynom {
        var res = Polynom()
        var listOfCalculations = mutableListOf<Double>()
        val min = minOf(other.degree(), list.lastIndex)
        val otherCoeffs = other.coeffs.toList()

        for (i in 0..min) {
            var x = i
            listOfCalculations = mutableListOf()
            listOfCalculations += (if (min == list.lastIndex) otherCoeffs.map { it * list.reversed()[i] }
            else list.map { it * otherCoeffs.reversed()[i] })
            while (x > 0) {
                listOfCalculations += 0.0
                x -= 1
            }
            res += Polynom(*listOfCalculations.toDoubleArray())
        }
        return res

    }


    /**
     * Деление
     *
     * Про операции деления и взятия остатка см. статью Википедии
     * "Деление многочленов столбиком". Основные свойства:
     *
     * Если A / B = C и A % B = D, то A = B * C + D и степень D меньше степени B
     */
    operator fun div(other: Polynom): Polynom {
        var answer = mutableListOf<Double>()
        var res = Polynom()

        if (this.degree() < other.degree()) return Polynom(0.0)

        var x = Polynom(*removeWhileZero(this.coeffs.toList()).toDoubleArray())
        val y = Polynom(*removeWhileZero(other.coeffs.toList()).toDoubleArray())
        var i = x.degree() - y.degree()
        while (i + 1 > 0) {
            var degreeOfAnswer = x.degree() - y.degree()
            x = Polynom(*removeWhileZero(x.coeffs.toList()).toDoubleArray())
            answer = mutableListOf<Double>()
            answer += x.coeffs.toList()[0] / y.coeffs.toList()[0]
            while (degreeOfAnswer > 0) {
                answer += 0.0
                degreeOfAnswer -= 1
            }
            res += Polynom(*answer.toDoubleArray())
            x -= y * Polynom(*answer.toDoubleArray())
            i -= 1
        }
        return res
    }

    /**
     * Взятие остатка
     */
    operator fun rem(other: Polynom): Polynom = this - (other * (this / other))

    /**
     * Сравнение на равенство
     */
    override fun equals(other: Any?): Boolean {
        if (other == null) return false
        var answer = true
        if (other is Polynom) {
            val min = minOf(other.degree(), list.lastIndex)
            for (i in 0..min) {
                if (other.coeff(i) != this.coeff(i)) answer = false
                if (min == other.degree() && other.degree() != list.lastIndex) {
                    for (z in min + 1..this.degree()) {
                        if (this.coeff(z) != 0.0) answer = false
                    }
                } else if (other.degree() != list.lastIndex) {
                    for (y in min + 1..other.degree()) {
                        if (other.coeff(y) != 0.0) answer = false
                    }
                }
            }
        } else return false
        return answer
    }

    /**
     * Получение хеш-кода
     */
    override fun hashCode(): Int {
        var result = coeffs.contentHashCode()
        result = 31 * result + list.hashCode()
        return result
    }

    fun removeWhileZero(list: List<Double>): List<Double> {
        val outList = list.toMutableList();

        for (item in list) {
            if (item == 0.0) outList.removeAt(0)
            else break;
        }

        return outList;
    }
}

