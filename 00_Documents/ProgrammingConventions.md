# JSesh Programming Conventions (as of 2020/03/04, will evolve)

This document is here to remind me (and future programmers) of JSesh Conventions
about naming and organisation.

## "Backing" packages

Packages named `backingSupport` contain technical code I decided to move to their own package for 
readability. They are not supposed to be public. In Java 9+, I will probably use the new Module
system to hide them.
