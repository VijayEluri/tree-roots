drop table links;
drop table files;
drop table audio;
drop table audio_links;
drop table spider_info;

create table links
(
    domain varChar(150) unique,
    url varChar(800) primary key,
    relevance tinyint not null default 1,
    remove char(1) not null default 'f'
);

create table files
(
	url varChar(800) unique,
	remove char(1) not null default 'f'
);


create table audio               
(
	id int unsigned primary key AUTO_INCREMENT,
	artist_name varChar(100),
	track_name varChar(100),
	artist_search_field varChar(300),
	track_search_field varChar(300),
	file_count int unsigned default 0,
	remove char(1) not null default 'f',
	is_new char(1) not null default 't'
);

create table audio_links
(
	audio_id int unsigned,
	domain varchar(150) not null,
	url varChar(800) unique,
	last_checked timestamp default now(),
	manually_changed char(1) default 'f',
	grey_listed char(1) default 'f',
	remove char(1) not null default 'f',
	is_new char(1) not null default 't',
	FOREIGN KEY (audio_id) REFERENCES audio(id),
);

create table spider_info
(
	spider_manager varChar(100),
	type varChar(15),
	id int,
	status varChar(50),
	running char(1),
	cur_domain varChar(200),
	last_checked timestamp
);

insert into links (domain, url, relevance) values 
('stereogum', 'http://stereogum.com/', 3),
('iheartcomix', 'http://www.iheartcomix.com/', 3),
('pitchforkmedia', 'http://www.pitchforkmedia.com/', 3),
('toomanysebastians', 'http://toomanysebastians.blogspot.com', 3),
('hypem', 'http://hypem.com/', 4);

/* add indexes */

commit;

/* prints out table sizes */
select (select count(*) from links where del = 'f') as links, (select count(*) from files where del = 'f') as files, (select count(*) from domains) as domains, (select count(*) from audio) as audio, (select count(*) from audio_links where del = 'f') as 'audio links' from dual;
/* recalculates  audio file counts */
update audio a set file_count = (select count(*) from audio_links where a.id = audio_id);
/* prints the artists track counts starting with the letter A */
select artist_name, count(*) from audio where artist_name like 'a%' group by artist_name order by artist_name;
/* export audio to csv */
SELECT id, convert(artist using ascii), convert(track using ascii),file_count INTO OUTFILE '/tmp/audio.csv' FIELDS TERMINATED BY ',' ESCAPED BY '"' OPTIONALLY ENCLOSED BY '"' LINES TERMINATED BY '\n' from Audio;
SELECT id, audio_id, convert(domain using ascii), convert(url using ascii) INTO OUTFILE '/tmp/audio_links.csv' FIELDS TERMINATED BY ',' ESCAPED BY '"' OPTIONALLY ENCLOSED BY '"' LINES TERMINATED BY '\n' from Audio_Links;


//login to remote api shell
python2.5 remote_api_shell.py -s mxtree.appspot.com mxtree

//delete audio

from loaders.models import Audio
from loaders.models import AudioLinks
import time

all_audio = Audio.all()
while True:
	entries = all_audio.fetch(50)
	print "deleting"
	db.delete(entries)
	time.sleep(0.5)


all_audio_links = AudioLinks.all()
while True:
	entries = all_audio_links.fetch(200)
	print "deleting"
	db.delete(entries)
	time.sleep(20)

//import data
python2.5 appcfg.py upload_data --noisy --config_file=loaders/audio_loader.py --filename=audio.csv --kind=Audio --db_filename=audio_upload --auth_domain=http://mxtree.appspot.com ../web2py
python2.5 appcfg.py upload_data --noisy --config_file=loaders/audio_links_loader.py --filename=audio_links.csv --kind=AudioLinks --db_filename=audio_links_upload --auth_domain=http://mxtree.appspot.com ../web2py


