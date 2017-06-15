package org.kframework.kore.parser

/** A scanner of an input stream.
  *
  * Should be initialized by [[init]] before use,
  * and closed by [[close]] after use.
  *
  * @constructor Creates a new scanner.
  */
class Scanner {

  private var stream: io.Source = _
  private var lines: Iterator[String] = _
  private var input: Iterator[Char] = _
  /** The string of the line that this scanner currently reads. */
  var line: String = _
  /** The line number of the current line. */
  var lineNum: Int = _
  /** The column position of this scanner in the line. */
  var columnNum: Int = _

  /** Initializes this scanner.
    *
    * @param src The stream to associate with this scanner.
    */
  def init(src: io.Source): Unit = {
    stream = src
    lines = stream.getLines()
    line = ""
    lineNum = 0
    columnNum = 0
    readLine()
  }

  /** Closes the stream associated with this scanner. */
  def close(): Unit = {
    stream.close()
  }

  @throws(classOf[java.io.EOFException])
  private def readLine(): Unit = {
    if (lines.hasNext) {
      line = lines.next()
      input = line.iterator
      lineNum += 1
      columnNum = 0
    } else {
      // end of file
      throw new java.io.EOFException()
    }
  }

  private var lookahead: Option[Char] = None
  private var yieldEOL: Boolean = false

  /** Returns the next character from the stream.
    *
    * Returns a blank space ' ' when a newline is encountered.
    */
  @throws(classOf[java.io.EOFException])
  def next(): Char = {
    columnNum += 1
    lookahead match {
      case Some(c) =>
        lookahead = None
        c
      case None =>
        if (input.hasNext) {
          input.next()
        } else if (!yieldEOL) {
          // end of line
          yieldEOL = true
          ' ' // safer than '\n' (platform independent)
        } else {
          yieldEOL = false
          readLine()
          next()
        }
    }
  }

  /** Puts back the character into the stream.
    *
    * Cannot put other characters until the inserted character has been read by [[next]].
    */
  def putback(c: Char): Unit = {
    columnNum -= 1
    lookahead match {
      case Some(_) => ???
      case None =>
        lookahead = Some(c)
    }
  }

  /** Consumes the whitespace characters until a non-whitespace character is met. */
  @throws(classOf[java.io.EOFException])
  def skipWhitespaces(): Unit = {
    next() match {
      case ' ' => skipWhitespaces()
      case '\t' => columnNum += 3; skipWhitespaces()
      case '\n' | '\r' => ??? // skipWhitespaces() // shouldn't be reachable.
      case c => putback(c)
    }
  }

  /** Returns the next non-whitespace character. */
  def nextWithSkippingWhitespaces(): Char = {
    skipWhitespaces()
    next()
  }

  /** Checks if EOF is reached.
    *
    * Side effect:
    * Consumes the whitespace characters until either a non-whitespace character or EOF is met.
    */
  def isEOF(): Boolean = {
    try {
      putback(nextWithSkippingWhitespaces())
      false
    } catch {
      case _: java.io.EOFException => true
      case _: Throwable => ??? // shouldn't be reachable
    }
  }

}
