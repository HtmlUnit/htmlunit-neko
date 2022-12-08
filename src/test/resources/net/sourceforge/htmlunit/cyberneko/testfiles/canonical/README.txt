The "canonical" output of the files in this directory are
a modified NSGMLS format, as described below. Each piece
of information is conveyed on a separate line, encoded in
UTF-8.

  startElement ::= '(' name
  attribute ::= 'A' name ' ' value
  endElement ::= ')' name
  characters ::= '"' text
  comment ::= '#' text

  text ::= Unicode chars, with tab, carriage return, and
           newline escaped as \t, \r, and \n, respectively.
  