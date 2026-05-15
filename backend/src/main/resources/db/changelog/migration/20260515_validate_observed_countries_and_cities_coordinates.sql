-- validation queries extracted from 20260515_seed_observed_countries_and_cities.sql
-- each statement returns the seeded coordinate for one locality

SELECT c.id, 'Гагра', 'Gagra', ST_SetSRID(ST_MakePoint(40.262264, 43.283252), 4326)
FROM countries c
WHERE c.code = 'AB';

SELECT c.id, 'Новый Афон', 'Novyy Afon', ST_SetSRID(ST_MakePoint(40.810546, 43.09017), 4326)
FROM countries c
WHERE c.code = 'AB';

SELECT c.id, 'Пицунда', 'Pitsunda', ST_SetSRID(ST_MakePoint(40.349641, 43.156427), 4326)
FROM countries c
WHERE c.code = 'AB';

SELECT c.id, 'Ереван', 'Yerevan', ST_SetSRID(ST_MakePoint(44.479468, 40.192603), 4326)
FROM countries c
WHERE c.code = 'AM';

SELECT c.id, 'Минск', 'Minsk', ST_SetSRID(ST_MakePoint(27.5667, 53.9), 4326)
FROM countries c
WHERE c.code = 'BY';

SELECT c.id, 'Гомель', 'Homyel’', ST_SetSRID(ST_MakePoint(30.9842, 52.4453), 4326)
FROM countries c
WHERE c.code = 'BY';

SELECT c.id, 'Витебск', 'Vitsyebsk', ST_SetSRID(ST_MakePoint(30.2056, 55.1917), 4326)
FROM countries c
WHERE c.code = 'BY';

SELECT c.id, 'Гродно', 'Hrodna', ST_SetSRID(ST_MakePoint(23.8333, 53.6667), 4326)
FROM countries c
WHERE c.code = 'BY';

SELECT c.id, 'Могилёв', 'Mahilyow', ST_SetSRID(ST_MakePoint(30.35, 53.9167), 4326)
FROM countries c
WHERE c.code = 'BY';

SELECT c.id, 'Брест', 'Brest', ST_SetSRID(ST_MakePoint(23.6569, 52.1347), 4326)
FROM countries c
WHERE c.code = 'BY';

SELECT c.id, 'Бобруйск', 'Babruysk', ST_SetSRID(ST_MakePoint(29.2333, 53.15), 4326)
FROM countries c
WHERE c.code = 'BY';

SELECT c.id, 'Барановичи', 'Baranavichy', ST_SetSRID(ST_MakePoint(26.0167, 53.1333), 4326)
FROM countries c
WHERE c.code = 'BY';

SELECT c.id, 'Город Борисов', 'Horad Barysaw', ST_SetSRID(ST_MakePoint(28.505, 54.2279), 4326)
FROM countries c
WHERE c.code = 'BY';

SELECT c.id, 'Пинск', 'Pinsk', ST_SetSRID(ST_MakePoint(26.1031, 52.1153), 4326)
FROM countries c
WHERE c.code = 'BY';

SELECT c.id, 'Мозырь', 'Mazyr', ST_SetSRID(ST_MakePoint(29.25, 52.05), 4326)
FROM countries c
WHERE c.code = 'BY';

SELECT c.id, 'Лида', 'Lida', ST_SetSRID(ST_MakePoint(25.2958, 53.8956), 4326)
FROM countries c
WHERE c.code = 'BY';

SELECT c.id, 'Орша', 'Orsha', ST_SetSRID(ST_MakePoint(30.4258, 54.5092), 4326)
FROM countries c
WHERE c.code = 'BY';

SELECT c.id, 'Солигорск', 'Salihorsk', ST_SetSRID(ST_MakePoint(27.5333, 52.8), 4326)
FROM countries c
WHERE c.code = 'BY';

SELECT c.id, 'Новополоцк', 'Navapolatsk', ST_SetSRID(ST_MakePoint(28.65, 55.5333), 4326)
FROM countries c
WHERE c.code = 'BY';

SELECT c.id, 'Молодечно', 'Maladzyechna', ST_SetSRID(ST_MakePoint(26.8572, 54.3208), 4326)
FROM countries c
WHERE c.code = 'BY';

SELECT c.id, 'Полоцк', 'Polatsk', ST_SetSRID(ST_MakePoint(28.8, 55.4833), 4326)
FROM countries c
WHERE c.code = 'BY';

SELECT c.id, 'Жлобин', 'Zhlobin', ST_SetSRID(ST_MakePoint(30.0333, 52.9), 4326)
FROM countries c
WHERE c.code = 'BY';

SELECT c.id, 'Речица', 'Rechytsa', ST_SetSRID(ST_MakePoint(30.3947, 52.3639), 4326)
FROM countries c
WHERE c.code = 'BY';

SELECT c.id, 'Город Жодино', 'Horad Zhodzina', ST_SetSRID(ST_MakePoint(28.35, 54.1), 4326)
FROM countries c
WHERE c.code = 'BY';

SELECT c.id, 'Светлогорск', 'Svyetlahorsk', ST_SetSRID(ST_MakePoint(29.7333, 52.6333), 4326)
FROM countries c
WHERE c.code = 'BY';

SELECT c.id, 'Слуцк', 'Slutsk', ST_SetSRID(ST_MakePoint(27.5667, 53.0333), 4326)
FROM countries c
WHERE c.code = 'BY';

SELECT c.id, 'Кобрин', 'Kobryn', ST_SetSRID(ST_MakePoint(24.3667, 52.2167), 4326)
FROM countries c
WHERE c.code = 'BY';

SELECT c.id, 'Слоним', 'Slonim', ST_SetSRID(ST_MakePoint(25.3167, 53.0833), 4326)
FROM countries c
WHERE c.code = 'BY';

SELECT c.id, 'Волковыск', 'Vawkavysk', ST_SetSRID(ST_MakePoint(24.4667, 53.1667), 4326)
FROM countries c
WHERE c.code = 'BY';

SELECT c.id, 'Калинковичи', 'Kalinkavichy', ST_SetSRID(ST_MakePoint(29.3333, 52.125), 4326)
FROM countries c
WHERE c.code = 'BY';

SELECT c.id, 'Сморгонь', 'Smarhon', ST_SetSRID(ST_MakePoint(26.4, 54.4836), 4326)
FROM countries c
WHERE c.code = 'BY';

SELECT c.id, 'Рогачёв', 'Rahachow', ST_SetSRID(ST_MakePoint(30.05, 53.1), 4326)
FROM countries c
WHERE c.code = 'BY';

SELECT c.id, 'Дзержинск', 'Dzyarzhynsk', ST_SetSRID(ST_MakePoint(27.1333, 53.6833), 4326)
FROM countries c
WHERE c.code = 'BY';

SELECT c.id, 'Осиповичи', 'Asipovichy', ST_SetSRID(ST_MakePoint(28.4756, 53.2933), 4326)
FROM countries c
WHERE c.code = 'BY';

SELECT c.id, 'Горки', 'Horki', ST_SetSRID(ST_MakePoint(30.9833, 54.2667), 4326)
FROM countries c
WHERE c.code = 'BY';

SELECT c.id, 'Берёза', 'Byaroza', ST_SetSRID(ST_MakePoint(24.9667, 52.55), 4326)
FROM countries c
WHERE c.code = 'BY';

SELECT c.id, 'Новогрудок', 'Navahrudak', ST_SetSRID(ST_MakePoint(25.8167, 53.5833), 4326)
FROM countries c
WHERE c.code = 'BY';

SELECT c.id, 'Вилейка', 'Vilyeyka', ST_SetSRID(ST_MakePoint(26.926, 54.498), 4326)
FROM countries c
WHERE c.code = 'BY';

SELECT c.id, 'Лунинец', 'Luninyets', ST_SetSRID(ST_MakePoint(26.8, 52.25), 4326)
FROM countries c
WHERE c.code = 'BY';

SELECT c.id, 'Кричев', 'Krychaw', ST_SetSRID(ST_MakePoint(31.7139, 53.7194), 4326)
FROM countries c
WHERE c.code = 'BY';

SELECT c.id, 'Ивацевичи', 'Ivatsevichy', ST_SetSRID(ST_MakePoint(25.3333, 52.7167), 4326)
FROM countries c
WHERE c.code = 'BY';

SELECT c.id, 'Город Смолевичи', 'Horad Smalyavichy', ST_SetSRID(ST_MakePoint(28.0667, 54.1), 4326)
FROM countries c
WHERE c.code = 'BY';

SELECT c.id, 'Марьина Горка', 'Mar’’ina Horka', ST_SetSRID(ST_MakePoint(28.1522, 53.5072), 4326)
FROM countries c
WHERE c.code = 'BY';

SELECT c.id, 'Пружаны', 'Pruzhany', ST_SetSRID(ST_MakePoint(24.4644, 52.5567), 4326)
FROM countries c
WHERE c.code = 'BY';

SELECT c.id, 'Поставы', 'Pastavy', ST_SetSRID(ST_MakePoint(26.8333, 55.1167), 4326)
FROM countries c
WHERE c.code = 'BY';

SELECT c.id, 'Добруш', 'Dobrush', ST_SetSRID(ST_MakePoint(31.3167, 52.4167), 4326)
FROM countries c
WHERE c.code = 'BY';

SELECT c.id, 'Фаниполь', 'Fanipal’', ST_SetSRID(ST_MakePoint(27.3333, 53.75), 4326)
FROM countries c
WHERE c.code = 'BY';

SELECT c.id, 'Глубокое', 'Hlybokaye', ST_SetSRID(ST_MakePoint(27.6833, 55.1333), 4326)
FROM countries c
WHERE c.code = 'BY';

SELECT c.id, 'Столбцы', 'Stowbtsy', ST_SetSRID(ST_MakePoint(26.7333, 53.4833), 4326)
FROM countries c
WHERE c.code = 'BY';

SELECT c.id, 'Заславль', 'Zaslawye', ST_SetSRID(ST_MakePoint(27.2847, 54.0083), 4326)
FROM countries c
WHERE c.code = 'BY';

SELECT c.id, 'Лепель', 'Lyepyel', ST_SetSRID(ST_MakePoint(28.6944, 54.875), 4326)
FROM countries c
WHERE c.code = 'BY';

SELECT c.id, 'Ошмяны', 'Ashmyany', ST_SetSRID(ST_MakePoint(25.9375, 54.425), 4326)
FROM countries c
WHERE c.code = 'BY';

SELECT c.id, 'Быхов', 'Bykhaw', ST_SetSRID(ST_MakePoint(30.25, 53.5167), 4326)
FROM countries c
WHERE c.code = 'BY';

SELECT c.id, 'Иваново', 'Ivanava', ST_SetSRID(ST_MakePoint(25.55, 52.1333), 4326)
FROM countries c
WHERE c.code = 'BY';

SELECT c.id, 'Житковичи', 'Zhytkavichy', ST_SetSRID(ST_MakePoint(27.8667, 52.2333), 4326)
FROM countries c
WHERE c.code = 'BY';

SELECT c.id, 'Несвиж', 'Nyasvizh', ST_SetSRID(ST_MakePoint(26.6667, 53.2167), 4326)
FROM countries c
WHERE c.code = 'BY';

SELECT c.id, 'Щучин', 'Shchuchyn', ST_SetSRID(ST_MakePoint(24.7333, 53.6167), 4326)
FROM countries c
WHERE c.code = 'BY';

SELECT c.id, 'Логойск', 'Lahoysk', ST_SetSRID(ST_MakePoint(27.85, 54.2), 4326)
FROM countries c
WHERE c.code = 'BY';

SELECT c.id, 'Климовичи', 'Klimavichy', ST_SetSRID(ST_MakePoint(31.95, 53.6167), 4326)
FROM countries c
WHERE c.code = 'BY';

SELECT c.id, 'Костюковичи', 'Kastsyukovichy', ST_SetSRID(ST_MakePoint(32.05, 53.3333), 4326)
FROM countries c
WHERE c.code = 'BY';

SELECT c.id, 'Шклов', 'Shklow', ST_SetSRID(ST_MakePoint(30.2864, 54.2236), 4326)
FROM countries c
WHERE c.code = 'BY';

SELECT c.id, 'Дрогичин', 'Drahichyn', ST_SetSRID(ST_MakePoint(25.15, 52.1833), 4326)
FROM countries c
WHERE c.code = 'BY';

SELECT c.id, 'Островец', 'Astravyets', ST_SetSRID(ST_MakePoint(25.9553, 54.6136), 4326)
FROM countries c
WHERE c.code = 'BY';

SELECT c.id, 'Мосты', 'Masty', ST_SetSRID(ST_MakePoint(24.55, 53.417), 4326)
FROM countries c
WHERE c.code = 'BY';

SELECT c.id, 'Жабинка', 'Zhabinka', ST_SetSRID(ST_MakePoint(24.0233, 52.2006), 4326)
FROM countries c
WHERE c.code = 'BY';

SELECT c.id, 'Столин', 'Stolin', ST_SetSRID(ST_MakePoint(26.85, 51.8833), 4326)
FROM countries c
WHERE c.code = 'BY';

SELECT c.id, 'Ганцевичи', 'Hantsavichy', ST_SetSRID(ST_MakePoint(26.4333, 52.75), 4326)
FROM countries c
WHERE c.code = 'BY';

SELECT c.id, 'Хойники', 'Khoyniki', ST_SetSRID(ST_MakePoint(29.9644, 51.8892), 4326)
FROM countries c
WHERE c.code = 'BY';

SELECT c.id, 'Малорита', 'Malaryta', ST_SetSRID(ST_MakePoint(24.0833, 51.7833), 4326)
FROM countries c
WHERE c.code = 'BY';

SELECT c.id, 'Микашевичи', 'Mikashevichy', ST_SetSRID(ST_MakePoint(27.4736, 52.2203), 4326)
FROM countries c
WHERE c.code = 'BY';

SELECT c.id, 'Лельчицы', 'Lyelchytsy', ST_SetSRID(ST_MakePoint(28.3214, 51.7894), 4326)
FROM countries c
WHERE c.code = 'BY';

SELECT c.id, 'Новолукомль', 'Novalukoml’', ST_SetSRID(ST_MakePoint(29.15, 54.6569), 4326)
FROM countries c
WHERE c.code = 'BY';

SELECT c.id, 'Городок', 'Haradok', ST_SetSRID(ST_MakePoint(30, 55.4667), 4326)
FROM countries c
WHERE c.code = 'BY';

SELECT c.id, 'Березино', 'Byerazino', ST_SetSRID(ST_MakePoint(28.9833, 53.8333), 4326)
FROM countries c
WHERE c.code = 'BY';

SELECT c.id, 'Любань', 'Lyuban', ST_SetSRID(ST_MakePoint(28.0525, 52.7819), 4326)
FROM countries c
WHERE c.code = 'BY';

SELECT c.id, 'Клецк', 'Klyetsk', ST_SetSRID(ST_MakePoint(26.6372, 53.0636), 4326)
FROM countries c
WHERE c.code = 'BY';

SELECT c.id, 'Белоозёрск', 'Byelaazyorsk', ST_SetSRID(ST_MakePoint(25.1667, 52.45), 4326)
FROM countries c
WHERE c.code = 'BY';

SELECT c.id, 'Старые Дороги', 'Staryya Darohi', ST_SetSRID(ST_MakePoint(28.265, 53.0394), 4326)
FROM countries c
WHERE c.code = 'BY';

SELECT c.id, 'Узда', 'Uzda', ST_SetSRID(ST_MakePoint(27.2244, 53.4661), 4326)
FROM countries c
WHERE c.code = 'BY';

SELECT c.id, 'Ляховичи', 'Lyakhavichy', ST_SetSRID(ST_MakePoint(26.2667, 53.0333), 4326)
FROM countries c
WHERE c.code = 'BY';

SELECT c.id, 'Червень', 'Chervyen', ST_SetSRID(ST_MakePoint(28.4322, 53.7078), 4326)
FROM countries c
WHERE c.code = 'BY';

SELECT c.id, 'Мачулищи', 'Machulishchy', ST_SetSRID(ST_MakePoint(27.5958, 53.7814), 4326)
FROM countries c
WHERE c.code = 'BY';

SELECT c.id, 'Петриков', 'Pyetrykaw', ST_SetSRID(ST_MakePoint(28.5, 52.1333), 4326)
FROM countries c
WHERE c.code = 'BY';

SELECT c.id, 'Барань', 'Baran', ST_SetSRID(ST_MakePoint(30.3333, 54.4833), 4326)
FROM countries c
WHERE c.code = 'BY';

SELECT c.id, 'Копыль', 'Kapyl', ST_SetSRID(ST_MakePoint(27.0917, 53.15), 4326)
FROM countries c
WHERE c.code = 'BY';

SELECT c.id, 'Воложин', 'Valozhyn', ST_SetSRID(ST_MakePoint(26.5167, 54.0833), 4326)
FROM countries c
WHERE c.code = 'BY';

SELECT c.id, 'Мстиславль', 'Mstsislaw', ST_SetSRID(ST_MakePoint(31.7167, 54.0167), 4326)
FROM countries c
WHERE c.code = 'BY';

SELECT c.id, 'Чаусы', 'Chavusy', ST_SetSRID(ST_MakePoint(30.9714, 53.8075), 4326)
FROM countries c
WHERE c.code = 'BY';

SELECT c.id, 'Белыничи', 'Byalynichy', ST_SetSRID(ST_MakePoint(29.7094, 53.9956), 4326)
FROM countries c
WHERE c.code = 'BY';

SELECT c.id, 'Скидель', 'Skidal’', ST_SetSRID(ST_MakePoint(24.2519, 53.5861), 4326)
FROM countries c
WHERE c.code = 'BY';

SELECT c.id, 'Толочин', 'Talachyn', ST_SetSRID(ST_MakePoint(29.7, 54.4167), 4326)
FROM countries c
WHERE c.code = 'BY';

SELECT c.id, 'Берёзовка', 'Byarozawka', ST_SetSRID(ST_MakePoint(25.5, 53.7167), 4326)
FROM countries c
WHERE c.code = 'BY';

SELECT c.id, 'Браслав', 'Braslaw', ST_SetSRID(ST_MakePoint(27.0318, 55.6391), 4326)
FROM countries c
WHERE c.code = 'BY';

SELECT c.id, 'Чечерск', 'Chachersk', ST_SetSRID(ST_MakePoint(30.9161, 52.9161), 4326)
FROM countries c
WHERE c.code = 'BY';

SELECT c.id, 'Ельск', 'Yelsk', ST_SetSRID(ST_MakePoint(29.15, 51.8167), 4326)
FROM countries c
WHERE c.code = 'BY';

SELECT c.id, 'Ветка', 'Vyetka', ST_SetSRID(ST_MakePoint(31.1833, 52.5667), 4326)
FROM countries c
WHERE c.code = 'BY';

SELECT c.id, 'Буда-Кошелёво', 'Buda-Kashalyova', ST_SetSRID(ST_MakePoint(30.5667, 52.7167), 4326)
FROM countries c
WHERE c.code = 'BY';

SELECT c.id, 'Крупки', 'Krupki', ST_SetSRID(ST_MakePoint(29.1333, 54.3167), 4326)
FROM countries c
WHERE c.code = 'BY';

SELECT c.id, 'Наровля', 'Narowlya', ST_SetSRID(ST_MakePoint(29.9644, 51.8892), 4326)
FROM countries c
WHERE c.code = 'BY';

SELECT c.id, 'Каменец', 'Kamyanyets', ST_SetSRID(ST_MakePoint(23.8167, 52.4), 4326)
FROM countries c
WHERE c.code = 'BY';

SELECT c.id, 'Кировск', 'Kirawsk', ST_SetSRID(ST_MakePoint(29.473, 53.2692), 4326)
FROM countries c
WHERE c.code = 'BY';

SELECT c.id, 'Корма', 'Karma', ST_SetSRID(ST_MakePoint(30.8106, 53.1292), 4326)
FROM countries c
WHERE c.code = 'BY';

SELECT c.id, 'Дятлово', 'Dzyatlava', ST_SetSRID(ST_MakePoint(25.4056, 53.4653), 4326)
FROM countries c
WHERE c.code = 'BY';

SELECT c.id, 'Чашники', 'Chashniki', ST_SetSRID(ST_MakePoint(29.1647, 54.8533), 4326)
FROM countries c
WHERE c.code = 'BY';

SELECT c.id, 'Славгород', 'Slawharad', ST_SetSRID(ST_MakePoint(31, 53.4333), 4326)
FROM countries c
WHERE c.code = 'BY';

SELECT c.id, 'Миоры', 'Myory', ST_SetSRID(ST_MakePoint(27.6167, 55.6167), 4326)
FROM countries c
WHERE c.code = 'BY';

SELECT c.id, 'Чериков', 'Cherykaw', ST_SetSRID(ST_MakePoint(31.3667, 53.5667), 4326)
FROM countries c
WHERE c.code = 'BY';

SELECT c.id, 'Кличев', 'Klichaw', ST_SetSRID(ST_MakePoint(29.3333, 53.4833), 4326)
FROM countries c
WHERE c.code = 'BY';

SELECT c.id, 'Круглое', 'Kruhlaye', ST_SetSRID(ST_MakePoint(29.7964, 54.2478), 4326)
FROM countries c
WHERE c.code = 'BY';

SELECT c.id, 'Октябрьский', 'Aktsyabrski', ST_SetSRID(ST_MakePoint(28.8833, 52.6472), 4326)
FROM countries c
WHERE c.code = 'BY';

SELECT c.id, 'Ивье', 'Iwye', ST_SetSRID(ST_MakePoint(25.7667, 53.9167), 4326)
FROM countries c
WHERE c.code = 'BY';

SELECT c.id, 'Сенно', 'Syanno', ST_SetSRID(ST_MakePoint(29.7, 54.8), 4326)
FROM countries c
WHERE c.code = 'BY';

SELECT c.id, 'Глусск', 'Hlusk', ST_SetSRID(ST_MakePoint(28.6922, 52.8895), 4326)
FROM countries c
WHERE c.code = 'BY';

SELECT c.id, 'Мядель', 'Myadzyel', ST_SetSRID(ST_MakePoint(26.9333, 54.8667), 4326)
FROM countries c
WHERE c.code = 'BY';

SELECT c.id, 'Дубровно', 'Dubrowna', ST_SetSRID(ST_MakePoint(30.6833, 54.5667), 4326)
FROM countries c
WHERE c.code = 'BY';

SELECT c.id, 'Верхнедвинск', 'Vyerkhnyadzvinsk', ST_SetSRID(ST_MakePoint(27.95, 55.7833), 4326)
FROM countries c
WHERE c.code = 'BY';

SELECT c.id, 'Докшицы', 'Dokshytsy', ST_SetSRID(ST_MakePoint(27.7667, 54.9), 4326)
FROM countries c
WHERE c.code = 'BY';

SELECT c.id, 'Лиозно', 'Lyozna', ST_SetSRID(ST_MakePoint(30.8, 55.0167), 4326)
FROM countries c
WHERE c.code = 'BY';

SELECT c.id, 'Радошковичи', 'Radashkovichy', ST_SetSRID(ST_MakePoint(27.2333, 54.15), 4326)
FROM countries c
WHERE c.code = 'BY';

SELECT c.id, 'Хотимск', 'Khotsimsk', ST_SetSRID(ST_MakePoint(32.5722, 53.4083), 4326)
FROM countries c
WHERE c.code = 'BY';

SELECT c.id, 'Шарковщина', 'Sharkawshchyna', ST_SetSRID(ST_MakePoint(27.4667, 55.3667), 4326)
FROM countries c
WHERE c.code = 'BY';

SELECT c.id, 'Лоев', 'Loyew', ST_SetSRID(ST_MakePoint(30.8, 51.9333), 4326)
FROM countries c
WHERE c.code = 'BY';

SELECT c.id, 'Свислочь', 'Svislach', ST_SetSRID(ST_MakePoint(24.1, 53.0333), 4326)
FROM countries c
WHERE c.code = 'BY';

SELECT c.id, 'Давид-Городок', 'Davyd-Haradok', ST_SetSRID(ST_MakePoint(27.2139, 52.0556), 4326)
FROM countries c
WHERE c.code = 'BY';

SELECT c.id, 'Краснополье', 'Krasnapollye', ST_SetSRID(ST_MakePoint(31.4022, 53.3333), 4326)
FROM countries c
WHERE c.code = 'BY';

SELECT c.id, 'Вороново', 'Voranava', ST_SetSRID(ST_MakePoint(25.3167, 54.15), 4326)
FROM countries c
WHERE c.code = 'BY';

SELECT c.id, 'Великая Берестовица', 'Vyalikaya Byerastavitsa', ST_SetSRID(ST_MakePoint(24.0208, 53.1956), 4326)
FROM countries c
WHERE c.code = 'BY';

SELECT c.id, 'Чисть', 'Chysts', ST_SetSRID(ST_MakePoint(27.1089, 54.2683), 4326)
FROM countries c
WHERE c.code = 'BY';

SELECT c.id, 'Высокое', 'Vysokaye', ST_SetSRID(ST_MakePoint(23.3806, 52.3686), 4326)
FROM countries c
WHERE c.code = 'BY';

SELECT c.id, 'Россоны', 'Rasony', ST_SetSRID(ST_MakePoint(28.8092, 55.9042), 4326)
FROM countries c
WHERE c.code = 'BY';

SELECT c.id, 'Брагин', 'Brahin', ST_SetSRID(ST_MakePoint(30.2667, 51.7833), 4326)
FROM countries c
WHERE c.code = 'BY';

SELECT c.id, 'Василевичи', 'Vasilyevichy', ST_SetSRID(ST_MakePoint(29.8, 52.2667), 4326)
FROM countries c
WHERE c.code = 'BY';

SELECT c.id, 'Дрибин', 'Drybin', ST_SetSRID(ST_MakePoint(31.0931, 54.1194), 4326)
FROM countries c
WHERE c.code = 'BY';

SELECT c.id, 'Туров', 'Turaw', ST_SetSRID(ST_MakePoint(27.74, 52.07), 4326)
FROM countries c
WHERE c.code = 'BY';

SELECT c.id, 'Коссово', 'Kosava', ST_SetSRID(ST_MakePoint(25.15, 52.75), 4326)
FROM countries c
WHERE c.code = 'BY';

SELECT c.id, 'Дисна', 'Dzisna', ST_SetSRID(ST_MakePoint(28.2167, 55.5667), 4326)
FROM countries c
WHERE c.code = 'BY';

SELECT c.id, 'Носилово', 'Nasilava', ST_SetSRID(ST_MakePoint(26.7789, 54.3094), 4326)
FROM countries c
WHERE c.code = 'BY';

SELECT c.id, 'Андреевщина', 'Andreyewshchyna', ST_SetSRID(ST_MakePoint(30.4521, 54.5689), 4326)
FROM countries c
WHERE c.code = 'BY';

SELECT c.id, 'Город Орша', 'Horad Orsha', ST_SetSRID(ST_MakePoint(30.4053, 54.5153), 4326)
FROM countries c
WHERE c.code = 'BY';

SELECT c.id, 'Город Речица', 'Horad Rechytsa', ST_SetSRID(ST_MakePoint(30.3947, 52.3639), 4326)
FROM countries c
WHERE c.code = 'BY';

SELECT c.id, 'Сухум', 'Sukhumi', ST_SetSRID(ST_MakePoint(41.02661, 43.000258), 4326)
FROM countries c
WHERE c.code = 'GE';

SELECT c.id, 'Астана', 'Astana', ST_SetSRID(ST_MakePoint(71.424027, 51.161404), 4326)
FROM countries c
WHERE c.code = 'KZ';

SELECT c.id, 'Костанай', 'Kostanay', ST_SetSRID(ST_MakePoint(63.63248, 53.212318), 4326)
FROM countries c
WHERE c.code = 'KZ';

SELECT c.id, 'Шымкент', 'Shymkent', ST_SetSRID(ST_MakePoint(69.58421, 42.318756), 4326)
FROM countries c
WHERE c.code = 'KZ';

SELECT c.id, 'Тересполь', 'Terespol', ST_SetSRID(ST_MakePoint(23.640801, 52.068554), 4326)
FROM countries c
WHERE c.code = 'PL';

SELECT c.id, 'Белград', 'Belgrade', ST_SetSRID(ST_MakePoint(20.460684, 44.816663), 4326)
FROM countries c
WHERE c.code = 'RS';

SELECT c.id, 'Абакан', 'Abakan', ST_SetSRID(ST_MakePoint(91.431597, 53.741383), 4326)
FROM countries c
WHERE c.code = 'RU';

SELECT c.id, 'Альметьевск', 'Almetevsk', ST_SetSRID(ST_MakePoint(52.302541, 54.899231), 4326)
FROM countries c
WHERE c.code = 'RU';

SELECT c.id, 'Анапа', 'Anapa', ST_SetSRID(ST_MakePoint(37.317605, 44.894975), 4326)
FROM countries c
WHERE c.code = 'RU';

SELECT c.id, 'Арамиль', 'Aramil', ST_SetSRID(ST_MakePoint(60.833706, 56.697398), 4326)
FROM countries c
WHERE c.code = 'RU';

SELECT c.id, 'Армавир', 'Armavir', ST_SetSRID(ST_MakePoint(41.129629, 44.999995), 4326)
FROM countries c
WHERE c.code = 'RU';

SELECT c.id, 'Архангельск', 'Arkhangelsk', ST_SetSRID(ST_MakePoint(40.574504, 64.550552), 4326)
FROM countries c
WHERE c.code = 'RU';

SELECT c.id, 'Балашиха', 'Balashikha', ST_SetSRID(ST_MakePoint(37.936944, 55.800094), 4326)
FROM countries c
WHERE c.code = 'RU';

SELECT c.id, 'Белогорск', 'Belogorsk', ST_SetSRID(ST_MakePoint(128.472332, 50.922151), 4326)
FROM countries c
WHERE c.code = 'RU';

SELECT c.id, 'Белореченск', 'Belorechensk', ST_SetSRID(ST_MakePoint(39.872353, 44.761839), 4326)
FROM countries c
WHERE c.code = 'RU';

SELECT c.id, 'Бийск', 'Biysk', ST_SetSRID(ST_MakePoint(85.230808, 52.544108), 4326)
FROM countries c
WHERE c.code = 'RU';

SELECT c.id, 'Бобров', 'Bobrov', ST_SetSRID(ST_MakePoint(40.036052, 51.096515), 4326)
FROM countries c
WHERE c.code = 'RU';

SELECT c.id, 'Бор', 'Bor', ST_SetSRID(ST_MakePoint(44.073932, 56.355667), 4326)
FROM countries c
WHERE c.code = 'RU';

SELECT c.id, 'Борисоглебск', 'Borisoglebsk', ST_SetSRID(ST_MakePoint(42.092404, 51.35758), 4326)
FROM countries c
WHERE c.code = 'RU';

SELECT c.id, 'Боровичи', 'Borovichi', ST_SetSRID(ST_MakePoint(33.910483, 58.389568), 4326)
FROM countries c
WHERE c.code = 'RU';

SELECT c.id, 'Брянск', 'Bryansk', ST_SetSRID(ST_MakePoint(34.347944, 53.237129), 4326)
FROM countries c
WHERE c.code = 'RU';

SELECT c.id, 'Волгоград', 'Volgograd', ST_SetSRID(ST_MakePoint(44.443663, 48.665895), 4326)
FROM countries c
WHERE c.code = 'RU';

SELECT c.id, 'Вольск', 'Volsk', ST_SetSRID(ST_MakePoint(47.386034, 52.049803), 4326)
FROM countries c
WHERE c.code = 'RU';

SELECT c.id, 'Выкса', 'Vyksa', ST_SetSRID(ST_MakePoint(42.185257, 55.32105), 4326)
FROM countries c
WHERE c.code = 'RU';

SELECT c.id, 'Вязьма', 'Vyazma', ST_SetSRID(ST_MakePoint(34.288444, 55.210449), 4326)
FROM countries c
WHERE c.code = 'RU';

SELECT c.id, 'Гатчина', 'Gatchina', ST_SetSRID(ST_MakePoint(30.104332, 59.559237), 4326)
FROM countries c
WHERE c.code = 'RU';

SELECT c.id, 'Гороховец', 'Gorokhovets', ST_SetSRID(ST_MakePoint(42.680804, 56.207565), 4326)
FROM countries c
WHERE c.code = 'RU';

SELECT c.id, 'Гусев', 'Gusev', ST_SetSRID(ST_MakePoint(22.201119, 54.59281), 4326)
FROM countries c
WHERE c.code = 'RU';

SELECT c.id, 'Данилов', 'Danilov', ST_SetSRID(ST_MakePoint(40.178439, 58.185812), 4326)
FROM countries c
WHERE c.code = 'RU';

SELECT c.id, 'Десногорск', 'Desnogorsk', ST_SetSRID(ST_MakePoint(33.284573, 54.15275), 4326)
FROM countries c
WHERE c.code = 'RU';

SELECT c.id, 'Дзержинск', 'Dzerzhinsk', ST_SetSRID(ST_MakePoint(43.456994, 56.297349), 4326)
FROM countries c
WHERE c.code = 'RU';

SELECT c.id, 'Дубна', 'Dubna', ST_SetSRID(ST_MakePoint(37.136929, 56.727406), 4326)
FROM countries c
WHERE c.code = 'RU';

SELECT c.id, 'Дюртюли', 'Dyurtyuli', ST_SetSRID(ST_MakePoint(54.854506, 55.489184), 4326)
FROM countries c
WHERE c.code = 'RU';

SELECT c.id, 'Екатеринбург', 'Ekaterinburg', ST_SetSRID(ST_MakePoint(60.545635, 56.864822), 4326)
FROM countries c
WHERE c.code = 'RU';

SELECT c.id, 'Жигулёвск', 'Zhigulyovsk', ST_SetSRID(ST_MakePoint(49.528583, 53.431524), 4326)
FROM countries c
WHERE c.code = 'RU';

SELECT c.id, 'Жуковский', 'Zhukovskiy', ST_SetSRID(ST_MakePoint(38.122529, 55.588738), 4326)
FROM countries c
WHERE c.code = 'RU';

SELECT c.id, 'Заволжье', 'Zavolzhe', ST_SetSRID(ST_MakePoint(43.392366, 56.644711), 4326)
FROM countries c
WHERE c.code = 'RU';

SELECT c.id, 'Зеленоград', 'Zelenograd', ST_SetSRID(ST_MakePoint(37.175135, 55.982699), 4326)
FROM countries c
WHERE c.code = 'RU';

SELECT c.id, 'Ижевск', 'Izhevsk', ST_SetSRID(ST_MakePoint(53.219694, 56.854032), 4326)
FROM countries c
WHERE c.code = 'RU';

SELECT c.id, 'Искитим', 'Iskitim', ST_SetSRID(ST_MakePoint(83.281291, 54.611487), 4326)
FROM countries c
WHERE c.code = 'RU';

SELECT c.id, 'Истра', 'Istra', ST_SetSRID(ST_MakePoint(36.857964, 55.903799), 4326)
FROM countries c
WHERE c.code = 'RU';

SELECT c.id, 'Ишим', 'Ishim', ST_SetSRID(ST_MakePoint(69.467335, 56.107724), 4326)
FROM countries c
WHERE c.code = 'RU';

SELECT c.id, 'Йошкар-Ола', 'Yoshkar-Ola', ST_SetSRID(ST_MakePoint(47.884289, 56.63317), 4326)
FROM countries c
WHERE c.code = 'RU';

SELECT c.id, 'Казань', 'Kazan', ST_SetSRID(ST_MakePoint(49.15267, 55.798425), 4326)
FROM countries c
WHERE c.code = 'RU';

SELECT c.id, 'Калининград', 'Kaliningrad', ST_SetSRID(ST_MakePoint(20.52522, 54.730152), 4326)
FROM countries c
WHERE c.code = 'RU';

SELECT c.id, 'Калязин', 'Kalyazin', ST_SetSRID(ST_MakePoint(37.839263, 57.23786), 4326)
FROM countries c
WHERE c.code = 'RU';

SELECT c.id, 'Каменск-Уральский', 'Kamensk-Uralskiy', ST_SetSRID(ST_MakePoint(61.92954, 56.390576), 4326)
FROM countries c
WHERE c.code = 'RU';

SELECT c.id, 'Кемерово', 'Kemerovo', ST_SetSRID(ST_MakePoint(86.088249, 55.3555), 4326)
FROM countries c
WHERE c.code = 'RU';

SELECT c.id, 'Кинешма', 'Kineshma', ST_SetSRID(ST_MakePoint(42.147572, 57.441012), 4326)
FROM countries c
WHERE c.code = 'RU';

SELECT c.id, 'Киреевск', 'Kireevsk', ST_SetSRID(ST_MakePoint(37.928913, 53.926484), 4326)
FROM countries c
WHERE c.code = 'RU';

SELECT c.id, 'Кисловодск', 'Kislovodsk', ST_SetSRID(ST_MakePoint(42.710526, 43.909986), 4326)
FROM countries c
WHERE c.code = 'RU';

SELECT c.id, 'Колпино', 'Kolpino', ST_SetSRID(ST_MakePoint(30.591323, 59.749047), 4326)
FROM countries c
WHERE c.code = 'RU';

SELECT c.id, 'Конаково', 'Konakovo', ST_SetSRID(ST_MakePoint(36.77721, 56.711549), 4326)
FROM countries c
WHERE c.code = 'RU';

SELECT c.id, 'Кондрово', 'Kondrovo', ST_SetSRID(ST_MakePoint(35.932103, 54.80815), 4326)
FROM countries c
WHERE c.code = 'RU';

SELECT c.id, 'Красногорск', 'Krasnogorsk', ST_SetSRID(ST_MakePoint(37.322767, 55.829025), 4326)
FROM countries c
WHERE c.code = 'RU';

SELECT c.id, 'Красноуфимск', 'Krasnoufimsk', ST_SetSRID(ST_MakePoint(57.770937, 56.615268), 4326)
FROM countries c
WHERE c.code = 'RU';

SELECT c.id, 'Крымск', 'Krymsk', ST_SetSRID(ST_MakePoint(37.986668, 44.934511), 4326)
FROM countries c
WHERE c.code = 'RU';

SELECT c.id, 'Куйбышев', 'Kuybyshev', ST_SetSRID(ST_MakePoint(78.326281, 55.447371), 4326)
FROM countries c
WHERE c.code = 'RU';

SELECT c.id, 'Кунгур', 'Kungur', ST_SetSRID(ST_MakePoint(56.944259, 57.428119), 4326)
FROM countries c
WHERE c.code = 'RU';

SELECT c.id, 'Курск', 'Kursk', ST_SetSRID(ST_MakePoint(36.187679, 51.746076), 4326)
FROM countries c
WHERE c.code = 'RU';

SELECT c.id, 'Лабытнанги', 'Labytnangi', ST_SetSRID(ST_MakePoint(66.375955, 66.66213), 4326)
FROM countries c
WHERE c.code = 'RU';

SELECT c.id, 'Лакинск', 'Lakinsk', ST_SetSRID(ST_MakePoint(39.975549, 56.020901), 4326)
FROM countries c
WHERE c.code = 'RU';

SELECT c.id, 'Лысково', 'Lyskovo', ST_SetSRID(ST_MakePoint(45.055629, 56.017133), 4326)
FROM countries c
WHERE c.code = 'RU';

SELECT c.id, 'Майкоп', 'Maykop', ST_SetSRID(ST_MakePoint(40.098517, 44.589473), 4326)
FROM countries c
WHERE c.code = 'RU';

SELECT c.id, 'Махачкала', 'Makhachkala', ST_SetSRID(ST_MakePoint(47.489923, 42.987026), 4326)
FROM countries c
WHERE c.code = 'RU';

SELECT c.id, 'Междуреченск', 'Mezhdurechensk', ST_SetSRID(ST_MakePoint(88.082576, 53.684849), 4326)
FROM countries c
WHERE c.code = 'RU';

SELECT c.id, 'Миасс', 'Miass', ST_SetSRID(ST_MakePoint(60.114698, 55.065832), 4326)
FROM countries c
WHERE c.code = 'RU';

SELECT c.id, 'Москва', 'Moscow', ST_SetSRID(ST_MakePoint(37.287234, 55.605509), 4326)
FROM countries c
WHERE c.code = 'RU';

SELECT c.id, 'Московский', 'Moskovsky', ST_SetSRID(ST_MakePoint(37.349355, 55.592617), 4326)
FROM countries c
WHERE c.code = 'RU';

SELECT c.id, 'Мурино', 'Murino', ST_SetSRID(ST_MakePoint(30.474928, 60.046925), 4326)
FROM countries c
WHERE c.code = 'RU';

SELECT c.id, 'Мценск', 'Mtsensk', ST_SetSRID(ST_MakePoint(36.565919, 53.292956), 4326)
FROM countries c
WHERE c.code = 'RU';

SELECT c.id, 'Нальчик', 'Nalchik', ST_SetSRID(ST_MakePoint(43.587652, 43.45927), 4326)
FROM countries c
WHERE c.code = 'RU';

SELECT c.id, 'Невинномысск', 'Nevinnomyssk', ST_SetSRID(ST_MakePoint(41.937069, 44.644796), 4326)
FROM countries c
WHERE c.code = 'RU';

SELECT c.id, 'Нижнекамск', 'Nizhnekamsk', ST_SetSRID(ST_MakePoint(51.820059, 55.642512), 4326)
FROM countries c
WHERE c.code = 'RU';

SELECT c.id, 'Новоалтайск', 'Novoaltaysk', ST_SetSRID(ST_MakePoint(83.933502, 53.419879), 4326)
FROM countries c
WHERE c.code = 'RU';

SELECT c.id, 'Новокузнецк', 'Novokuznetsk', ST_SetSRID(ST_MakePoint(87.15063, 53.758549), 4326)
FROM countries c
WHERE c.code = 'RU';

SELECT c.id, 'Новосибирск', 'Novosibirsk', ST_SetSRID(ST_MakePoint(82.918026, 55.041808), 4326)
FROM countries c
WHERE c.code = 'RU';

SELECT c.id, 'Ногинск', 'Noginsk', ST_SetSRID(ST_MakePoint(38.442098, 55.852201), 4326)
FROM countries c
WHERE c.code = 'RU';

SELECT c.id, 'Озёры', 'Ozyory', ST_SetSRID(ST_MakePoint(38.557567, 54.853537), 4326)
FROM countries c
WHERE c.code = 'RU';

SELECT c.id, 'Октябрьский', 'Oktyabrskiy', ST_SetSRID(ST_MakePoint(53.469961, 54.48854), 4326)
FROM countries c
WHERE c.code = 'RU';

SELECT c.id, 'Орёл', 'Oryol', ST_SetSRID(ST_MakePoint(36.068584, 52.972649), 4326)
FROM countries c
WHERE c.code = 'RU';

SELECT c.id, 'Оренбург', 'Orenburg', ST_SetSRID(ST_MakePoint(55.073989, 51.773652), 4326)
FROM countries c
WHERE c.code = 'RU';

SELECT c.id, 'Петушки', 'Petushki', ST_SetSRID(ST_MakePoint(39.463267, 55.924903), 4326)
FROM countries c
WHERE c.code = 'RU';

SELECT c.id, 'Пионерский', 'Pionerskiy', ST_SetSRID(ST_MakePoint(20.225029, 54.949329), 4326)
FROM countries c
WHERE c.code = 'RU';

SELECT c.id, 'Протвино', 'Protvino', ST_SetSRID(ST_MakePoint(37.222657, 54.877201), 4326)
FROM countries c
WHERE c.code = 'RU';

SELECT c.id, 'Псков', 'Pskov', ST_SetSRID(ST_MakePoint(28.358519, 57.802347), 4326)
FROM countries c
WHERE c.code = 'RU';

SELECT c.id, 'Пугачев', 'Pugachev', ST_SetSRID(ST_MakePoint(48.813944, 52.010122), 4326)
FROM countries c
WHERE c.code = 'RU';

SELECT c.id, 'Пушкин', 'Pushkin', ST_SetSRID(ST_MakePoint(30.418715, 59.713549), 4326)
FROM countries c
WHERE c.code = 'RU';

SELECT c.id, 'Пущино', 'Pushchino', ST_SetSRID(ST_MakePoint(37.632521, 54.832818), 4326)
FROM countries c
WHERE c.code = 'RU';

SELECT c.id, 'Россошь', 'Rossosh', ST_SetSRID(ST_MakePoint(39.577637, 50.19513), 4326)
FROM countries c
WHERE c.code = 'RU';

SELECT c.id, 'Ростов', 'Rostov', ST_SetSRID(ST_MakePoint(39.415949, 57.184266), 4326)
FROM countries c
WHERE c.code = 'RU';

SELECT c.id, 'Рязань', 'Ryazan', ST_SetSRID(ST_MakePoint(39.764649, 54.634636), 4326)
FROM countries c
WHERE c.code = 'RU';

SELECT c.id, 'Самара', 'Samara', ST_SetSRID(ST_MakePoint(50.245422, 53.278459), 4326)
FROM countries c
WHERE c.code = 'RU';

SELECT c.id, 'Санкт-Петербург', 'Saint Petersburg', ST_SetSRID(ST_MakePoint(29.277595, 60.474196), 4326)
FROM countries c
WHERE c.code = 'RU';

SELECT c.id, 'Саранск', 'Saransk', ST_SetSRID(ST_MakePoint(45.1814, 54.174869), 4326)
FROM countries c
WHERE c.code = 'RU';

SELECT c.id, 'Саров', 'Sarov', ST_SetSRID(ST_MakePoint(43.33069, 54.935979), 4326)
FROM countries c
WHERE c.code = 'RU';

SELECT c.id, 'Светлогорск', 'Svetlogorsk', ST_SetSRID(ST_MakePoint(20.159589, 54.933407), 4326)
FROM countries c
WHERE c.code = 'RU';

SELECT c.id, 'Севастополь', 'Sevastopol', ST_SetSRID(ST_MakePoint(33.529135, 44.594531), 4326)
FROM countries c
WHERE c.code = 'RU';

SELECT c.id, 'Сергач', 'Sergach', ST_SetSRID(ST_MakePoint(45.49437, 55.519712), 4326)
FROM countries c
WHERE c.code = 'RU';

SELECT c.id, 'Серпухов', 'Serpukhov', ST_SetSRID(ST_MakePoint(37.411421, 54.913684), 4326)
FROM countries c
WHERE c.code = 'RU';

SELECT c.id, 'Симферополь', 'Simferopol', ST_SetSRID(ST_MakePoint(34.091595, 44.944987), 4326)
FROM countries c
WHERE c.code = 'RU';

SELECT c.id, 'Сланцы', 'Slantsy', ST_SetSRID(ST_MakePoint(28.086461, 59.118508), 4326)
FROM countries c
WHERE c.code = 'RU';

SELECT c.id, 'Слободской', 'Slobodskoy', ST_SetSRID(ST_MakePoint(50.181551, 58.721338), 4326)
FROM countries c
WHERE c.code = 'RU';

SELECT c.id, 'Сортавала', 'Sortavala', ST_SetSRID(ST_MakePoint(30.944184, 61.388597), 4326)
FROM countries c
WHERE c.code = 'RU';

SELECT c.id, 'Сосновый Бор', 'Sosnovyy Bor', ST_SetSRID(ST_MakePoint(29.086244, 59.910784), 4326)
FROM countries c
WHERE c.code = 'RU';

SELECT c.id, 'Сочи', 'Sochi', ST_SetSRID(ST_MakePoint(40.310183, 43.625163), 4326)
FROM countries c
WHERE c.code = 'RU';

SELECT c.id, 'Старая Русса', 'Staraya Russa', ST_SetSRID(ST_MakePoint(31.36438, 57.993591), 4326)
FROM countries c
WHERE c.code = 'RU';

SELECT c.id, 'Суздаль', 'Suzdal', ST_SetSRID(ST_MakePoint(40.441331, 56.431975), 4326)
FROM countries c
WHERE c.code = 'RU';

SELECT c.id, 'Сызрань', 'Syzran', ST_SetSRID(ST_MakePoint(48.480291, 53.169933), 4326)
FROM countries c
WHERE c.code = 'RU';

SELECT c.id, 'Сыктывкар', 'Syktyvkar', ST_SetSRID(ST_MakePoint(50.834177, 61.667135), 4326)
FROM countries c
WHERE c.code = 'RU';

SELECT c.id, 'Таганрог', 'Taganrog', ST_SetSRID(ST_MakePoint(38.945288, 47.203606), 4326)
FROM countries c
WHERE c.code = 'RU';

SELECT c.id, 'Тейково', 'Teykovo', ST_SetSRID(ST_MakePoint(40.535029, 56.851831), 4326)
FROM countries c
WHERE c.code = 'RU';

SELECT c.id, 'Тимашевск', 'Timashevsk', ST_SetSRID(ST_MakePoint(38.939568, 45.628175), 4326)
FROM countries c
WHERE c.code = 'RU';

SELECT c.id, 'Тихвин', 'Tikhvin', ST_SetSRID(ST_MakePoint(33.534969, 59.644604), 4326)
FROM countries c
WHERE c.code = 'RU';

SELECT c.id, 'Троицк', 'Troitsk', ST_SetSRID(ST_MakePoint(37.288669, 55.454378), 4326)
FROM countries c
WHERE c.code = 'RU';

SELECT c.id, 'Тула', 'Tula', ST_SetSRID(ST_MakePoint(37.499633, 54.413427), 4326)
FROM countries c
WHERE c.code = 'RU';

SELECT c.id, 'Тутаев', 'Tutaev', ST_SetSRID(ST_MakePoint(39.541683, 57.881559), 4326)
FROM countries c
WHERE c.code = 'RU';

SELECT c.id, 'Тюмень', 'Tyumen', ST_SetSRID(ST_MakePoint(65.535311, 57.151626), 4326)
FROM countries c
WHERE c.code = 'RU';

SELECT c.id, 'Узловая', 'Uzlovaya', ST_SetSRID(ST_MakePoint(38.158047, 53.976043), 4326)
FROM countries c
WHERE c.code = 'RU';

SELECT c.id, 'Усть-Лабинск', 'Ust-Labinsk', ST_SetSRID(ST_MakePoint(39.689431, 45.215164), 4326)
FROM countries c
WHERE c.code = 'RU';

SELECT c.id, 'Уфа', 'Ufa', ST_SetSRID(ST_MakePoint(55.985556, 54.750968), 4326)
FROM countries c
WHERE c.code = 'RU';

SELECT c.id, 'Чапаевск', 'Chapaevsk', ST_SetSRID(ST_MakePoint(49.746677, 52.989281), 4326)
FROM countries c
WHERE c.code = 'RU';

SELECT c.id, 'Челябинск', 'Chelyabinsk', ST_SetSRID(ST_MakePoint(61.399827, 55.163731), 4326)
FROM countries c
WHERE c.code = 'RU';

SELECT c.id, 'Череповец', 'Cherepovets', ST_SetSRID(ST_MakePoint(37.928552, 59.123938), 4326)
FROM countries c
WHERE c.code = 'RU';

SELECT c.id, 'Черкесск', 'Cherkessk', ST_SetSRID(ST_MakePoint(42.047311, 44.227244), 4326)
FROM countries c
WHERE c.code = 'RU';

SELECT c.id, 'Чита', 'Chita', ST_SetSRID(ST_MakePoint(113.499581, 52.033012), 4326)
FROM countries c
WHERE c.code = 'RU';

SELECT c.id, 'Шатура', 'Shatura', ST_SetSRID(ST_MakePoint(39.521316, 55.57369), 4326)
FROM countries c
WHERE c.code = 'RU';

SELECT c.id, 'Шелехов', 'Shelekhov', ST_SetSRID(ST_MakePoint(104.083688, 52.205373), 4326)
FROM countries c
WHERE c.code = 'RU';

SELECT c.id, 'Шуя', 'Shuya', ST_SetSRID(ST_MakePoint(41.39126, 56.857337), 4326)
FROM countries c
WHERE c.code = 'RU';

SELECT c.id, 'Щёлкино', 'Shchyolkino', ST_SetSRID(ST_MakePoint(35.822069, 45.429443), 4326)
FROM countries c
WHERE c.code = 'RU';

SELECT c.id, 'Щербинка', 'Shcherbinka', ST_SetSRID(ST_MakePoint(37.568088, 55.499285), 4326)
FROM countries c
WHERE c.code = 'RU';

SELECT c.id, 'Элиста', 'Elista', ST_SetSRID(ST_MakePoint(44.264148, 46.308318), 4326)
FROM countries c
WHERE c.code = 'RU';

SELECT c.id, 'Южно-Сахалинск', 'Yuzhno-Sakhalinsk', ST_SetSRID(ST_MakePoint(142.752994, 46.965357), 4326)
FROM countries c
WHERE c.code = 'RU';

SELECT c.id, 'Юрга', 'Yurga', ST_SetSRID(ST_MakePoint(84.928494, 55.713093), 4326)
FROM countries c
WHERE c.code = 'RU';

SELECT c.id, 'Юрьев-Польский', 'Yuryev-Polsky', ST_SetSRID(ST_MakePoint(39.668526, 56.484024), 4326)
FROM countries c
WHERE c.code = 'RU';

SELECT c.id, 'Якутск', 'Yakutsk', ST_SetSRID(ST_MakePoint(129.728985, 62.029183), 4326)
FROM countries c
WHERE c.code = 'RU';

SELECT c.id, 'Яровое', 'Yarovoye', ST_SetSRID(ST_MakePoint(78.563671, 52.922509), 4326)
FROM countries c
WHERE c.code = 'RU';

SELECT c.id, 'Ярцево', 'Yartsevo', ST_SetSRID(ST_MakePoint(32.771675, 55.081878), 4326)
FROM countries c
WHERE c.code = 'RU';

SELECT c.id, 'Бурса', 'Bursa', ST_SetSRID(ST_MakePoint(29.139934, 40.120027), 4326)
FROM countries c
WHERE c.code = 'TR';

SELECT c.id, 'Гебзе', 'Gebze', ST_SetSRID(ST_MakePoint(29.4298, 40.7988), 4326)
FROM countries c
WHERE c.code = 'TR';

SELECT c.id, 'Дарыджа', 'Darica', ST_SetSRID(ST_MakePoint(29.3793, 40.7565), 4326)
FROM countries c
WHERE c.code = 'TR';

SELECT c.id, 'Орхангази', 'Orhangazi', ST_SetSRID(ST_MakePoint(29.309461, 40.490713), 4326)
FROM countries c
WHERE c.code = 'TR';

SELECT c.id, 'Стамбул', 'Istanbul', ST_SetSRID(ST_MakePoint(29.208879, 40.918202), 4326)
FROM countries c
WHERE c.code = 'TR';

SELECT c.id, 'Харманджик', 'Harmancik', ST_SetSRID(ST_MakePoint(29.1477, 39.6787), 4326)
FROM countries c
WHERE c.code = 'TR';

SELECT c.id, 'Чайырова', 'Cayirova', ST_SetSRID(ST_MakePoint(29.4204, 40.8353), 4326)
FROM countries c
WHERE c.code = 'TR';

SELECT c.id, 'Шиле', 'Shile', ST_SetSRID(ST_MakePoint(29.412808, 41.202708), 4326)
FROM countries c
WHERE c.code = 'TR';

SELECT c.id, 'Килия', 'Kiliya', ST_SetSRID(ST_MakePoint(29.269513, 45.43594), 4326)
FROM countries c
WHERE c.code = 'UA';

SELECT c.id, 'Малин', 'Malyn', ST_SetSRID(ST_MakePoint(29.236416, 50.774176), 4326)
FROM countries c
WHERE c.code = 'UA';

SELECT c.id, 'Мариуполь', 'Mariupol', ST_SetSRID(ST_MakePoint(37.543672, 47.096903), 4326)
FROM countries c
WHERE c.code = 'UA';

SELECT c.id, 'Погребище', 'Pohrebyshche', ST_SetSRID(ST_MakePoint(29.258574, 49.48453), 4326)
FROM countries c
WHERE c.code = 'UA';

SELECT c.id, 'Ташкент', 'Tashkent', ST_SetSRID(ST_MakePoint(69.250743, 41.285858), 4326)
FROM countries c
WHERE c.code = 'UZ';
SELECT c.id, 'Фергана', 'Fergana', ST_SetSRID(ST_MakePoint(71.785431, 40.389803), 4326)
FROM countries c
WHERE c.code = 'UZ';
