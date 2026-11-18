# Oportunidades de Refabricação

Ficha laboratorial nº 4, Parte 2. Cada membro aplica 7 refabricações de tipos diferentes, no seu ramo `Refactoring_XXXXXX`.

| Local | Cheiro no código | Refabricação | Nº aluno |
|---|---|---|---|
| `Tasks::menu` (casos `RAJADA` e `SIMULA`) | Long Method / Switch Statements | Extract Method | 129822 |
| `Tasks::menu` (bloco "fim de jogo" repetido em `RAJADA` e `SIMULA`) | Duplicated Code | Find and Replace Code Duplicates | 129822 |
| `Tasks::GOODBYE_MESSAGE`, `Main::MAX_SHIPS` | Speculative Generality (código morto) | Safe Delete | 129822 |
| `Tasks::menu` (`3000`, `"help"`, `"quit"`) | Magic Numbers / Strings | Introduce Constant | 129822 |
| `Tasks::buildFleet`, `Tasks::readShip` (variáveis `s`, `c`, `i`) | Nomes pouco expressivos | Rename | 129822 |
| `Tasks::buildFleet` (variável `success`) | Variável temporária redundante | Inline Variable | 129822 |
| `Tasks::menu` (`I18n.getLocale()` repetido no caso `IDIOMA`) | Expressão repetida | Introduce Variable | 129822 |
| `Fleet::getFloatingShips`, `Fleet::getSunkShips`, `Fleet::getShipsLike` | Duplicated Code | Extract Method | 129836 |
| `Frigate` → `Ship` (método de preenchimento linear extraído) | Duplicated Code (entre subclasses) | Pull Members Up | 129836 |
| `Caravel`, `Carrack` (construtores) | Duplicated Code | Find and Replace Code Duplicates | 129836 |
| `Ship::positions` (campo `protected`) | Data exposure / Inappropriate Intimacy | Encapsulate Fields | 129836 |
| `Fleet::printAllShips`, `Fleet::printFloatingShips`, `Fleet::printShipsByCategory` | Speculative Generality (código morto) | Safe Delete | 129836 |
| `Fleet::colisionRisk` | Nome com erro ortográfico | Rename | 129836 |
| `Ship::isInsideBoard` | Condição complexa | Introduce Variable | 129836 |
| `Move::processEnemyFire` (construção da mensagem e do JSON) | Long Method | Extract Method | 129859 |
| `Move::processEnemyFire` (`validShots`, `repeatedShots`, `missedShots`, `outsideShots`) | Long Parameter List / Data Clumps | Introduce Parameter Object | 129859 |
| `Move::processEnemyFire` (`result.ship()` repetido) | Expressão repetida / Message Chains | Introduce Variable | 129859 |
| `Move::processEnemyFire` (chaves `"validShots"`, `"sunkBoats"`, ...) | Magic Strings | Introduce Constant | 129859 |
| `Move::processEnemyFire` (`cordsPerBoat`, `cord`) | Nomes pouco expressivos | Rename | 129859 |
| `Move::durationMillis` + getters/setter | Speculative Generality (código morto) | Safe Delete | 129859 |
| `Galleon::fillWest` / `Galleon::fillEast` | Duplicated Code | Introduce Parameter | 129859 |
| `Game::randomEnemyFire` | Long Method / Duplicated Code | Extract Method | 129868 |
| `Game::printBoard`, `printShips`, `printBoardShots`, `printBoardFrame` → nova classe `BoardPrinter` | Large Class | Move Members | 129868 |
| `Game::printBoard`, `printMyBoard`, `printAlienBoard` (`show_shots`, `show_legend`, `ship_pos`) | Nomes fora da convenção | Rename | 129868 |
| `Game` (campos `countInvalidShots`, `countRepeatedShots`, `countHits`, `countSinks`) | Boxing desnecessário (`Integer`) | Type Migration | 129868 |
| `Game::jsonShots` (variável `jsonString`) | Atribuição redundante | Inline Variable | 129868 |
| `Position::adjacentPositions` (array `directions`) | Valor constante recriado a cada chamada | Introduce Constant | 129868 |
| `Game::printBoardShots` (`map[row][col]` repetido) | Expressão repetida | Introduce Variable | 129868 |
