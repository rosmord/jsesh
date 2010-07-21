-- Test database for hieroglyphs

create cached table glyph (
	code varchar(10) primary key,
	data varchar(10240)
);
