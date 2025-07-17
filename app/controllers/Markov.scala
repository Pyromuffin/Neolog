package controllers

import java.nio.file.{Files, OpenOption, Paths, StandardOpenOption}
import scala.math.pow

// load up a file and find the probabilities
object Markov {

  def GetLetterCounts(path : String) : Array[Array[Int]] = {

    val wordFile = scala.io.Source.fromFile(path)
    val words = wordFile.getLines()

    val letters = Array("a", "b", "c", "d", "e", "f", "g", "h", "i", "j", "k", "l", "m", "n", "o", "p", "q", "r", "s", "t", "u", "v", "w", "x", "y", "z");
    val counts = Array.ofDim[Array[Int]](26)

    for(i <- 0 until 26) {
      for (j <- 0 until 26) {
        counts(j) = Array.ofDim[Int](26)
        counts(j)(i) = 0
      }
    }

    val letterOffset = 'a'.toInt

    for(word <- words){
      val length = word.length
      for(i <- 0 until length -1) {
        val ch = word(i).toLower - letterOffset
        val next = word(i + 1).toLower - letterOffset

        counts(ch)(next) += 1
      }
    }

    counts
  }


  def GetDigramIndex(digram : (Int,Int) ) : Int =  {
    digram._1 + digram._2 * 26
  }

  def GetDigramString(index : Int) : String = {
    val letters = Array("a", "b", "c", "d", "e", "f", "g", "h", "i", "j", "k", "l", "m", "n", "o", "p", "q", "r", "s", "t", "u", "v", "w", "x", "y", "z");


    val first = letters(index % 26)
    val second  = letters(index / 26)


    s"$first$second"
  }

  def GetDigrams(word : String) : Array[Int] = {
    val letterOffset = 'a'.toInt

    val diagramCount = word.length - 1

    val digrams = Array.ofDim[Int](diagramCount)
    for(i <- 0 until diagramCount) {
      val first = word(i).toLower -letterOffset
      val second = word(i + 1).toLower -letterOffset
      digrams(i) = GetDigramIndex((first, second))
    }

    digrams
  }

  def GetDigramCounts(path : String) : Array[Array[Int]] = {

    val wordFile = scala.io.Source.fromFile(path)
    val words = wordFile.getLines()

    val digramSize = 26 * 26

    val counts = Array.ofDim[Array[Int]](digramSize)

    for(i <- 0 until digramSize) {
      for (j <- 0 until digramSize) {
        counts(j) = Array.ofDim[Int](digramSize)
        counts(j)(i) = 0
      }
    }

    for(word <- words){
      val digramTransitionCount = word.length - 3

      if(digramTransitionCount > 0) {
        val digrams = GetDigrams(word)

        for(i <- 0 until digramTransitionCount) {
          val firstDigram = digrams(i)
          val secondDigram = digrams(i + 2)
          counts(firstDigram)(secondDigram) += 1
        }
      }
    }

    counts
  }

  var g_probabilities:  Array[Array[Double]] = null
  var g_digramProbabilities:  Array[Array[Double]] = null


  def CalculateDigramProbabilities(counts : Array[Array[Int]]) : Array[Array[Double]] = {



    val digramSize = 26 * 26

    val probability = Array.ofDim[Array[Double]](digramSize)


    for(i <- 0 until digramSize) {
      probability(i) = Array.ofDim[Double](digramSize)

      val digramCounts = counts(i)
      val total = digramCounts.sum
      for(j <- 0 until digramSize) {
        probability(i)(j) = digramCounts(j) / total.toDouble

      }
    }

    g_digramProbabilities = probability
    probability
  }


  def CalculateProbabilities(counts : Array[Array[Int]]) : Array[Array[Double]] = {

    val probability = Array.ofDim[Array[Double]](26)


    for(i <- 0 until 26) {
      probability(i) = Array.ofDim[Double](26)

      val letterCounts = counts(i)
      val total = letterCounts.sum
      for(j <- 0 until 26) {
        probability(i)(j) = letterCounts(j) / total.toDouble

      }
    }

    g_probabilities = probability
    probability
  }

  def CalculateTonitrus(neologism : String) : Double = {
    // i guess calculate the average transition probability?
    val length = neologism.length
    val transitionCount = length -1
    val letterOffset = 'a'.toInt

    var totalProbability = 0.0

    for(i <- 0 until transitionCount) {
      val ch = neologism(i).toLower
      val next = neologism(i + 1).toLower

      if(ch == ' ' || next == ' ') {

      } else {
        val prob = g_probabilities(ch - letterOffset)(next - letterOffset)
        totalProbability += prob
      }
    }

    totalProbability / transitionCount
  }

  def CalculateDoubleTonitrus(neologism : String) : Double = {



    val digramSize = 26 * 26
    val length = neologism.length
    val transitionCount = length -3



    if(transitionCount < 0){
      return 0
    }

    var totalProbability = 0.0
    val digrams = GetDigrams(neologism)
    val digramsStrings = GetDigrams(neologism).map(GetDigramString)

    //println(s"the digrams for $neologism are ${digramsStrings.mkString("Array(", ", ", ")")}")

    for(i <- 0 until transitionCount) {

      val first = digrams(i)
      val second = digrams(i + 2)

      if(first < 0 || first >= digramSize  || second < 0 || second >= digramSize) {

      } else {
        val prob = g_digramProbabilities(first)(second)
        totalProbability += prob
      }
    }

    totalProbability / transitionCount
  }


  def GetNgramIndex(ngram : Array[Char]) : Int = {
    val offset = 'a'.toInt
    val length = ngram.length
    var index = 0
    for(i <- 0 until length){
      index *= 28
      index += (ngram(i).toInt - offset)
    }

    index
  }


  case class NGramData(ngramLength : Int) {
    private val ngramSize = scala.math.pow(28, ngramLength).toInt

    val counts = Array.ofDim[Array[Int]](ngramSize)

    for (j <- 0 until ngramSize) {
      counts(j) = Array.ofDim[Int](28)
      for(i <- 0 until 28) {
        counts(j)(i) = 0
      }
    }

    var totalNgrams = 0
  }

  val g_ngramData = Array.ofDim[NGramData](10)

  def GetNgramCounts(path : String, ngramLength : Int) : NGramData = {

    val data = NGramData(ngramLength)

    val wordFile = scala.io.Source.fromFile(path)
    val words = wordFile.getLines()

    // { and | are the characters that come after z on the ascii table. we will use them to indicate start and end of words

    val prefix = Array.ofDim[Char](ngramLength)
    for(i <- 0 until ngramLength) {
      prefix(i) = '{'
    }

    val letters = Array("a", "b", "c", "d", "e", "f", "g", "h", "i", "j", "k", "l", "m", "n", "o", "p", "q", "r", "s", "t", "u", "v", "w", "x", "y", "z","<",">");

    val letterOffset = 'a'.toInt

    for(_word <- words){
      val word = _word.toCharArray.prependedAll(prefix).appended('|')
      val length = word.length

      for(i <- 0 until length - ngramLength) {

        val ngram = word.slice(i, i + ngramLength)
        val ngramIndex = GetNgramIndex(ngram)
        val next = word(i + ngramLength).toInt - letterOffset

        data.counts(ngramIndex)(next) += 1
        data.totalNgrams += 1
      }
    }

    g_ngramData(ngramLength) = data
    data
  }


  def GetStringFromIndex(_index : Int, ngramLength : Int) : String = {
    val letters = Array("a", "b", "c", "d", "e", "f", "g", "h", "i", "j", "k", "l", "m", "n", "o", "p", "q", "r", "s", "t", "u", "v", "w", "x", "y", "z","<",">");
    var str = ""
    var index = _index
    for(i <- 0 until ngramLength){
      str += letters(index % 28 )
      index /= 28
    }

    str
  }

  def CalculateTonitrus(_word : String, ngramLength : Int) : Double = {

    val letters = Array("a", "b", "c", "d", "e", "f", "g", "h", "i", "j", "k", "l", "m", "n", "o", "p", "q", "r", "s", "t", "u", "v", "w", "x", "y", "z","<",">");

    //println(s"----------------------------------------------------")
   // println(s"calculating tonitrus for $_word with ngram length $ngramLength ")

    val letterOffset = 'a'.toInt
    val prefix = Array.ofDim[Char](ngramLength)

    for(i <- 0 until ngramLength) {
      prefix(i) = '{'
    }
    val word = _word.toCharArray.prependedAll(prefix).appended('|')

    //println(s"ended: ${word.mkString("")}")

    val length = word.length
    val data = g_ngramData(ngramLength)
    var score = 0.0

    var bolts = 0

    for(i <- 0 until length - ngramLength) {

      val ngram = word.slice(i, i + ngramLength)
      val ngramIndex = GetNgramIndex(ngram)
      val ngramString = GetStringFromIndex(ngramIndex, ngramLength)

      val next = word(i + ngramLength).toInt - letterOffset

      val count = data.counts(ngramIndex)(next)


      if(ngram(ngramLength-1) == '{'  )
        score += 0
      else if(count != 0)
        score += count
      else bolts += 1

      //println(s"ngram: $ngramString, next: ${letters(next)}, count: $count")

    }

    score /= _word.length
    score /= data.totalNgrams
    score /= pow(10, bolts)

    //println(s"score: $score")

    score

  }

  def SuperTonitrus(word : String) : Double = {
    var sum = 0.0
    for(i <- 0 until 4) {
      sum += CalculateTonitrus(word, i + 1)  * pow(4, i)
    }

    sum * 100000
  }


  case class WordStatistics(averageScore : Double, count : Int) {
    val cutoff = 300.0
  }



  def GetWordStatistics(path : String) : WordStatistics = {

    val scorePath = Paths.get("/home/kelly/scores.csv")


    val wordFile = scala.io.Source.fromFile(path)
    val words = wordFile.getLines()

    var score = 0.0
    var wordCount = 0

    for(word <- words){
      val wordScore = SuperTonitrus(word)
      score += wordScore
      val str : String = s"$word, $wordScore\n"
      Files.writeString(scorePath, str, StandardOpenOption.APPEND )
      wordCount += 1
    }

    score /= wordCount
    println(s"average score is $score")

    WordStatistics(score, wordCount)
  }

}


