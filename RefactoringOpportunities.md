# Refactoring Opportunities

| Local | Code Smell | Refabricação | Aluno |
| :--- | :--- | :--- | :--- |
| `Tasks::menu` | Busy wait (`Call to Thread.sleep() in a loop`) | Extract Method | 111396 |
| `Tasks::menu` | Nullability and data flow (`printStatus` may produce NPE) | Introduce Variable | 111396 |
| `Tasks::menu` | Nullability and data flow (`printStatus` may produce NPE) | Find and Replace Code Duplicates | 111396 |
| `Game::randomEnemyFire` | Performance (`Call to 'set.removeAll(list)' may work slowly`) | Rename | 111396 |
| `Game::randomEnemyFire` | Performance (`Call to 'set.removeAll(list)' may work slowly`) | Make Static | 111396 |
| `Game::jsonShots` | Unused assignment (`Variable jsonString initializer null is redundant`) | Inline | 111396 |