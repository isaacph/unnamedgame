:command C :tabedit | :term ++curwin ++shell mvn compile ; mvn package
:command R :tabedit | :term ++curwin ++shell mvn exec:java@game
:command Rs :tabedit | :term ++curwin ++shell mvn exec:java@server
:command D :tabedit | :term ++curwin ++shell mvnDebug exec:java@game

