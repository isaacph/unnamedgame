# unnamedgame - 2D Multiplayer Turn-Based Strategy Game

## Feature overview
* TCP communication interface
* Server-side authoritative
* Game and rendering info are configurable from JSON files, loaded into game data using reflection
* Basic OpenGL renderer
## Feature breakdown
* TCP communication interface
  * Built on top of Java's DataInputStream/DataOutputStream classes (insecure)
  * Wrapper class for each type of "payload" that gets sent from client to server and vice versa
    * Example: EndTurn.java - serializes to a TeamID containing whose turn to end, validates by checking it's a valid team and currently has a turn to end
  * Unbounded number of clients, single server
  * Player login/logout feature (each player is called a Team in this project)
  * Simple chat messaging
* Server-side authoritative
  * All commands validate that the player *can* do them before they actually execute any actions on the server
  * All successful commands are relayed to all users
  * Server's world state is resent to all players if/when needed
* Game info is generated from a JSON file
  * Called gamedata.json
  * Defines the "model" for the world, meaning all things that the server would care about but the client wouldn't
    * For example, we include information about every ability a "Builder" has, but don't include information about how to draw the builder or his abilities
  * Allows hot-reloading the entire game definition
  * Allows sending the entire game definition over a connection and reloading it, allowing balance changes to be made *during* gameplay, for testing of course ;)
  * Uses reflection to fill up "model" classes from JSON
    * Avoids repetitive serializer and deserializer code
    * Safer than using DataInputStream (as the TCP networking currently does...)
    * Allows backwards compatibility (unlike DataInputStream)
* Rendering info is generated from a separate JSON file
  * Called visualdata.json
  * Visual data is recorded for each unit type's "name", so that unit type has to exist in the gamedata.json file for the visualdata.json definition to do anything
  * Allows hot-reloading from asset changes, so artists can see what their art looks like in the game itself by just running a reload command
* Basic OpenGL renderer
  * Based on OpenGL 2.0 (OpenGL 3.1 features commmented out for compatibility)
  * Basic "textured square" primitive
  * Tile grid optimization
    * Builds a VBO per "chunk" storing all tile information from the terrain (which tiles are grass vs road vs empty)
    * Renders only chunks that are on screen
    * Rebuilds chunks only when changes are made (only during level editing, not during gameplay)
    * Converts 2D tile-grid data into isometric view using some fancy matrix multiplication
  * TTF fonts
    * TTF fonts are loaded and packed by LWJGL's STB port
    * Packed font coordinates for each character are stored into a Vertex Buffer Object (VBO) (6 vertices per character)
    * At rendering time, the 6 VBO indices are indexed into based on the char code, and the specific glyph texture is thus chosen
    * Metrics loaded from STB and multiplied at rendering time
* Chat console with commands
  * Level editing command interface
    * Change the game to edit mode allows you to place world objects
    * If your player has permission, you can force the world to take your level's custom state
    * Can save/load level data to/from JSON (see the random "asdf.json" files for example levels)
## Dependencies
* Java
* LWJGL - window management, rendering
* org.json - JSON parsing/serialization
* Maven - dependency management
