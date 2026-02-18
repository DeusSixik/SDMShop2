# SDM Shop 2

*SDM Shop 2* is a complete, ground-up rewrite of the original store modification for Minecraft. Built with performance, flexibility, and security in mind,
it abandons the legacy codebase in favor of a modern Entity Component System (ECS) architecture.

## Why the Rewrite?

The original SDM Shop existed for over two years. Written when experience was limited, the codebase accumulated significant technical debt. 
The latest versions (7.x) still contained fragments from the very first iterations, making maintenance a struggle against broken legacy code.

### Key reasons for SDM Shop 2:
1. *Eliminating Technical Debt:* Instead of applying patches to an unstable foundation, the core has been rebuilt to ensure long-term stability.
2. *Library Independence:* The reliance on *FTB Library* caused constant compatibility issues due to frequent breaking changes. We have migrated to LDLib for a stable and powerful UI framework.
3. *Data Structure:* The old OOP approach resulted in chaotic SNBT files that were difficult for users to edit manually.

> [!NOTE]
> I apologize that it took nearly 3 years to deliver a truly stable and optimized mod. This rewrite represents the quality standard I always intended to provide.

## Key Features

### ECS Architecture (Entity Component System)
Unlike the traditional OOP structure, SDM Shop 2 uses ECS.
- *Flexibility:* Store entries are no longer rigid objects. They are entities composed of various components (Cost, Reward, Conditions).
- *Modularity:* You can easily mix and match components (e.g., a shop entry that costs items and XP, but only appears at night).
- *Configurability:* Store files are now clean and human-readable. You can modify shop data directly in the files without launching the game.

## Unmatched Performance
The previous version suffered from server freezes during purchases or synchronization because it sent the entire shop database to every client.
- *Smart Sync:* SDM Shop 2 uses *Lazy Loading*. The client only receives the data it currently needs (e.g., the specific tab being viewed).
- *Packet Splitting:* Large data transfers are split into small chunks, preventing connection timeouts and server lag.

## Enhanced Security
- *Server Authority:* Sensitive data (such as command rewards, permissions, or admin-only metadata) is *never* sent to the client unless necessary.
- *Permissions:* Clients cannot view or access shop entries they do not have permission for.

## Tech Stack
SDM Shop 2 leverages modern libraries to provide the best experience:

- [LDLib](https://www.curseforge.com/minecraft/mc-mods/ldlib) - The backbone of the new UI. Chosen for its rendering capabilities and stability compared to FTB Library.
- [Caffeine](https://github.com/ben-manes/caffeine) - A high-performance caching library used for efficient data management and quick access to player accounts.
- [Shadow Config](https://www.curseforge.com/minecraft/mc-mods/shadow-config) - A robust library for handling configuration files.

---
*Developed with* ❤️ *by* Sixik