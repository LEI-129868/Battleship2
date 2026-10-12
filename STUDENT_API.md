# Battleship Opponent — Student API Guide

The opponent server is already running at `http://localhost:8080`.
Your job is to write a Java player that talks to it.

---

## The 3-step game loop

```
1. Register yourself          →  POST /register
2. Send your shots            →  POST /game/{gameId}/shots
3. Receive the AI's shots     ←  POST {your-server}/game/{gameId}/shots   (AI calls YOU)
   Repeat steps 2-3 until game over.
```

---

## What YOU must implement

You need **one HTTP endpoint** on your own server:

```
POST /game/{gameId}/shots
```

The AI calls this with its shots. You receive them, apply them to your board,
and return the results. That's it.

---

## Message Formats

### m0 — Register  `POST /register`

**Request body:**
```json
{
  "playerName": "YourName",
  "callbackUrl": "http://your-ip:9090"
}
```

**Response:**
```json
{
  "gameId":      "550e8400-e29b-41d4-a716-446655440000",
  "status":      "REGISTERED",
  "shotsPerTurn": 3,
  "message":     "Welcome! Game ready. You shoot first."
}
```

Save the `gameId` — you need it for every other call.

---

### m1a — Your shots  `POST /game/{gameId}/shots`

Send exactly **3 shots** per turn. Positions use classic notation:
- `row`:    a letter `"A"` .. `"J"`
- `column`: a number `1` .. `10`

**Request body:**
```json
{
  "shots": [
    { "row": "B", "column": 3  },
    { "row": "G", "column": 7  },
    { "row": "D", "column": 10 }
  ]
}
```

---

### m1b — Results of your shots  (response to m1a)

```json
{
  "results": [
    { "row": "B", "column": 3,  "outcome": "MISS"              },
    { "row": "G", "column": 7,  "outcome": "HIT",  "shipType": "caravela" },
    { "row": "D", "column": 10, "outcome": "SUNK", "shipType": "galeao"   }
  ],
  "shipsRemaining": 9,
  "gameStatus": "ONGOING",
  "winner": null
}
```

| `outcome`  | Meaning                                        |
|------------|------------------------------------------------|
| `MISS`     | Water — no ship there                          |
| `HIT`      | Hit a ship, but it's still floating            |
| `SUNK`     | Last hit — that ship is now sunk               |
| `REPEATED` | You already fired at this position             |
| `INVALID`  | Position outside the board                     |

When `gameStatus` is `"GAME_OVER"`, `winner` will be `"STUDENT_WINS"` or `"AI_WINS"`.

---

### m2a — AI's shots  `POST {callbackUrl}/game/{gameId}/shots`

The AI will call your server with the same format as m1a:
```json
{
  "shots": [
    { "row": "A", "column": 1 },
    { "row": "C", "column": 5 },
    { "row": "H", "column": 8 }
  ]
}
```

---

### m2b — Your response to the AI's shots  (your server responds to m2a)

Return the results using the same format as m1b:
```json
{
  "results": [
    { "row": "A", "column": 1, "outcome": "HIT",  "shipType": "barca"    },
    { "row": "C", "column": 5, "outcome": "MISS"                          },
    { "row": "H", "column": 8, "outcome": "SUNK", "shipType": "caravela" }
  ],
  "shipsRemaining": 8,
  "gameStatus": "ONGOING",
  "winner": null
}
```

---

## Ship types in this game

| Type        | Size | Count |
|-------------|------|-------|
| `galeao`    | 5    | 1     |
| `fragata`   | 4    | 1     |
| `nau`       | 3    | 2     |
| `caravela`  | 2    | 3     |
| `barca`     | 1    | 4     |

**Total: 11 ships.**  Game ends when all 11 of one fleet are sunk.

---

## Quick test with curl

```bash
# 1. Register
curl -X POST http://localhost:8080/register \
     -H "Content-Type: application/json" \
     -d '{"playerName":"Test","callbackUrl":"http://host.docker.internal:9090"}'

# 2. Send shots (replace GAME_ID)
curl -X POST http://localhost:8080/game/GAME_ID/shots \
     -H "Content-Type: application/json" \
     -d '{"shots":[{"row":"A","column":1},{"row":"B","column":2},{"row":"C","column":3}]}'
```