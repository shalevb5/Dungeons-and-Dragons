# Dungeons & Dragons Board Game (Java)

This project is a Java implementation of a single-player, multi-level Dungeons & Dragons style board game.  
It was developed as part of an Object-Oriented Software Design assignment.

## Game Overview
- The player is trapped in a dungeon filled with enemies.
- The goal is to fight through the enemies and advance across multiple levels until victory.
- The game ends if the player dies or successfully clears all levels.

## Features
- **Turn-based gameplay**: Each game tick, the player acts first, then all enemies act.
- **Multiple player classes**:
  - Warrior (melee fighter with cooldown-based ability)
  - Mage (mana-based ranged caster)
  - Rogue (energy-based assassin)
  - *(Bonus: Hunter class with ranged attacks)*
- **Enemies**:
  - Monsters that move and chase the player
  - Traps that alternate between visible/invisible states
  - *(Bonus: Boss enemies with special abilities)*
- **Combat System**: Randomized attack/defense rolls determine battle outcomes.
- **Level progression**: Gain experience, level up, and improve stats as you defeat enemies.
- **Board rendering**: Each level is represented as a 2D grid with walls, free spaces, enemies, and the player.

## Interfaces
- **CLI (Command-Line Interface)**: Renders the board in ASCII with stats and combat logs.
- **GUI (optional extension)**: Can be integrated with JavaFX for a more visual experience.

## Input
- The game loads levels from text files (e.g., `level1.txt`, `level2.txt`).
- Characters on the board:
  - `.` → Free tile  
  - `#` → Wall  
  - `@` → Player  
  - `X` → Dead player  
  - Other symbols represent enemies.

---
