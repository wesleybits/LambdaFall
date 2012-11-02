# LambdaFall calculator

This is a lambda calculus interpreter, and was 
something that I did several months back to learn
the features of Scala, and something that I still
like to play with from time to time.  It requires
Processing's core library in order to function,
since that's what I used to code it's GUI.  You
can get that from 
[Processing's website](http://processing.org)

Using it is easy:

Upon starting it up, you'll see a happy colon
at the top and several co-centric rings of 
circles slowly prominading around their shared
midpoint.  Typing in a number and pressing enter
will cause the interface to go through a quick
convultion and yield the number and it's Church
numeral printed vertically along the side.
Great, huh?

## An Example:

Type this: 
`> succ := ^n^f^x (n f x)`

Then type:
`(succ 3)`

The output will be:
`(4, ^f^x (f (f (f (f x)))))`

You see, the `>` operator will assign a symbol
a meaning to the global environment, so instad 
of having to type `^n^f^x (n f x)` every time
you want to compute `n + 1`, you can assign
it a symbol and use that symbol instead.

This example also shows you the rest of the syntax:

`^<symbol> <expression>` starts a body.

`(<expression> <expression> ...)` applies some 
arguments to an expression.

A `<symbol>` is any alphabetic string.

All of these count as `<expression>`s.

## Details:

This calculator features implicit currying and 
lexical scope.  It has no tail-call optimisation.
It is call-by-value, so be very weary when using it;
the Y-Combinator will end your session very quickly,
for example.

I also have no concrete plans for this program, so 
suggestions and fixes are welcome and very much 
desired.

## For More Lambda Calculus

The [Wikipedia article](http://en.wikipedia.org/wiki/Lambda_calculus)
is a good start as any. It also links to some
great resources on the Internet.  

## Afterward

If there are problems in using my calculator while 
following along with the reading, please let me know 
by posting an issue to [this git](https://github.com/wesleybits/LambdaFall)
and I'll do my best to fix the problem.