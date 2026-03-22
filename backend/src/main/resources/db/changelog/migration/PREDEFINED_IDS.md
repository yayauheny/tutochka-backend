 Predefined UUIDs for reference data

Reference data for Minsk metro and seed data uses fixed UUIDs so migrations and manual scripts (e.g. after 2GIS import) can rely on stable IDs. All migrations and the standalone script `20251205_upsert_minsk_metro.sql` must use these same values.

## Countries

| Code | ID |
|------|-----|
| BY (Беларусь) | `00000000-0000-0000-0000-0000000000b1` |

## Cities

| name_ru | ID |
|---------|-----|
| Минск | `00000000-0000-0000-0000-0000000000c1` |

## Subway lines (Minsk)

| name_ru | short_code | ID |
|---------|------------|-----|
| Московская линия | 1 | `55665eb9-1efa-469f-bd14-058e35c4bf75` |
| Автозаводская линия | 2 | `270518e5-df16-4c42-bf9f-21f2f3a8dcbb` |
| Зеленолужская линия | 3 | `b250dde1-d7cf-486c-8f21-1a3246d4e5a0` |

## Subway stations (Minsk) — name_ru → ID

### Line 1 (Московская)
| name_ru | ID |
|---------|-----|
| Малиновка | `ea88119c-73d2-4ac2-8341-774295b3e617` |
| Петровщина | `c4a8b989-f59a-4319-82e3-23061330dcab` |
| Михалово | `642381d1-7748-4768-b081-8168e33965c5` |
| Грушевка | `1f26f874-0cdd-4a9b-bd24-8d6089860d00` |
| Институт культуры | `232915cf-4280-486c-a0f8-6e3c0459af31` |
| Площадь Ленина | `3b3b81bd-bbb9-4040-ac75-3816e975a576` |
| Октябрьская | `8964216a-a08a-4c1c-ac5c-ebbf24466bbe` |
| Площадь Победы | `5e69997b-d6d6-4fb6-b5d7-4acaf422b910` |
| Площадь Якуба Коласа | `eac4ead4-83d1-4cba-8a54-b860f4ed0ff8` |
| Академия наук | `4b4c046a-c7d2-46ba-8971-9c1491ec4adb` |
| Парк Челюскинцев | `f3806018-1e41-470d-97af-4e0ae1145bb1` |
| Московская | `00fddd00-11fc-4723-b284-077bc3d339b7` |
| Восток | `8ee907e3-1e87-4390-a365-a67c5b31103c` |
| Борисовский тракт | `8f8bd6cc-7c29-45da-a171-d83babb03473` |
| Уручье | `ce27977b-964d-4996-a0e8-298c1776691b` |

### Line 2 (Автозаводская)
| name_ru | ID |
|---------|-----|
| Каменная горка | `05cd10b6-3179-4924-99db-3931f2fb82d4` |
| Кунцевщина | `f932094b-15ae-419e-9e03-4d3dbaca3ad8` |
| Спортивная | `ae3cdf73-48ff-4b5c-b5b5-d8706a02b081` |
| Пушкинская | `ab4f8dcd-bf45-4e4a-bea2-bc1ec2c17015` |
| Молодежная | `c81fcae4-0248-4a9c-897e-470ce2ed163d` |
| Фрунзенская | `ec2f32b2-26b9-470d-ba02-d1ca314d9639` |
| Немига | `06ea7137-eb28-4060-ad60-8eb03f1d4a0d` |
| Купаловская | `5d402db6-a05f-411d-9888-137162dd6847` |
| Первомайская | `4af6a446-c3d1-4363-b5b3-978312ca50b2` |
| Пролетарская | `6a017c22-7595-4a29-978b-e016e1634cf3` |
| Тракторный завод | `c52d621c-77c4-4f23-aa2a-2cf045e39481` |
| Партизанская | `a69804ad-867f-4783-b753-9e00c96651d4` |
| Автозаводская | `1d33b1ce-f25d-4e7b-bad7-0eb146bef1da` |
| Могилевская | `b42aaa6a-cb5d-4542-b3dc-3d5641efa851` |

### Line 3 (Зеленолужская)
| name_ru | ID |
|---------|-----|
| Ковальская слобода | `87bb0369-a4b5-4d97-9e54-441952919642` |
| Вокзальная | `f3f404f3-1dfe-451e-8193-727e60685b80` |
| Площадь Франтишка Богушевича | `dd997017-c1a1-496e-addd-8c3bfabd86e1` |
| Юбилейная площадь | `f35b29e1-2303-42cf-a88e-380b33eadfc7` |
| Аэродромная | `3da81292-d4c7-4a9d-b28e-d46e2949d065` |
| Неморшанский сад | `ef0d2d07-3122-4207-b531-3fab5e7721b9` |
| Слуцкий гостинец | `17ae2b9d-0566-441e-8bc2-359bb9795f94` |

## Buildings (seed)

| address / description | ID |
|-----------------------|-----|
| Минск, Привокзальная площадь, 5 (Минский железнодорожный вокзал) | `00000000-0000-0000-0000-0000000000d1` |

## Notes

- The standalone script `20251205_upsert_minsk_metro.sql` is not in the Liquibase changelog; it is for manual refresh of station data (e.g. coordinates, external_ids from Yandex) and must use the same line and station IDs as `20251205_import_minsk_metro.sql`.
- When adding new stations or lines, add the same predefined UUIDs to this file, `import_minsk_metro.sql`, and `upsert_minsk_metro.sql`.
