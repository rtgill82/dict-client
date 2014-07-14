CREATE TABLE dict_servers (
  id INTEGER PRIMARY KEY AUTOINCREMENT,
  url TEXT NOT NULL,
  description TEXT DEFAULT NULL,
  readonly BOOLEAN DEFAULT 0,
  last_db_refresh DATETIME DEFAULT NULL
);

CREATE TABLE dictionaries (
  dict_server_id INTEGER NOT NULL,
  database TEXT NOT NULL,
  description TEXT DEFAULT NULL,

  FOREIGN KEY (dict_server_id)
    REFERENCES dict_servers(id)
);

INSERT INTO dict_servers
      SELECT NULL AS id,
'dict://dict.org' AS url,
'The standard dictd server, contains several databases related to English language and the FreeDict databases' AS description,
1 AS readonly,
NULL AS last_db_refresh
UNION SELECT NULL,
'dict://www.lojban.org',
'To and from lojban and a number of other languages',
0, NULL
UNION SELECT NULL,
'dict://dict.arabeyes.org',
'English-Arabic, extensible',
0, NULL
UNION SELECT NULL,
'dict://dict.saugus.net',
'The contents of the Saugus.net computer knowledgebase, including a section on computer terms, filename extensions, and free software titles',
0, NULL
UNION SELECT NULL,
'dict://dict.mova.org',
'Diverse databases, many Slavic/Russian dictionaries',
0, NULL
UNION SELECT NULL,
'dict://dict.uni-leipzig.de',
'Official FreeDict server, most current, OpenSearch description',
0, NULL
UNION SELECT NULL,
'dict://dict.dvo.ru',
'Standard databases, russian, ru-en, en-ru dictionaries',
0, NULL
UNION SELECT NULL,
'dict://indica-et-buddhica.org:2629',
'Sanskrit-Tibetan Buddhist Terminology, Monier-Williams Sanskrit-English Dictionary',
0, NULL
UNION SELECT NULL,
'dict://dict.hewgill.com',
'Formatted entry pages from the Wiktionary project. Offers four databases with varying levels of detail, including a raw wikitext view',
0, NULL
UNION SELECT NULL,
'dict://dict.bibleonline.ru',
'This DICT server Contains The Christian Material.',
0, NULL
UNION SELECT NULL,
'dict://la-sorciere.de',
'Debian terminology dictionaries, standard databases, eng<->deu',
0, NULL
UNION SELECT NULL,
'dict://nb.mblondel.org',
'A server focusing on free Japanese dictionaries',
0, NULL
UNION SELECT NULL,
'dict://dict.seesslen.net',
'fd-deu-eng, fd-eng-deu, fd-eng-rus',
0, NULL
UNION SELECT NULL,
'dict://dict.antono.info',
'Esperanto dictionary',
0, NULL
ORDER BY readonly DESC;
