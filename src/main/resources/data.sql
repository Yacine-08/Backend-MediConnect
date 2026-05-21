INSERT INTO region (nom) VALUES
                             ('Dakar'),
                             ('Thiès'),
                             ('Saint-Louis'),
                             ('Diourbel'),
                             ('Kaolack'),
                             ('Fatick'),
                             ('Kolda'),
                             ('Ziguinchor'),
                             ('Sédhiou'),
                             ('Tambacounda'),
                             ('Matam'),
                             ('Louga'),
                             ('Kaffrine'),
                             ('Kédougou');


INSERT INTO departement (nom, region_id) VALUES
-- =====================
-- DAKAR (1)
-- =====================
    ('Dakar', 1),
    ('Guédiawaye', 1),
    ('Pikine', 1),
    ('Rufisque', 1),

-- =====================
-- THIÈS (2)
-- =====================
    ('Thiès', 2),
    ('Mbour', 2),
    ('Tivaouane', 2),

-- =====================
-- SAINT-LOUIS (3)
-- =====================
    ('Saint-Louis', 3),
    ('Dagana', 3),
    ('Podor', 3),

-- =====================
-- DIOURBEL (4)
-- =====================
    ('Diourbel', 4),
    ('Bambey', 4),
    ('Mbacké', 4),

-- =====================
-- KAOLACK (5)
-- =====================
    ('Kaolack', 5),
    ('Guinguinéo', 5),
    ('Nioro du Rip', 5),

-- =====================
-- FATICK (6)
-- =====================
    ('Fatick', 6),
    ('Foundiougne', 6),
    ('Gossas', 6),

-- =====================
-- KOLDA (7)
-- =====================
    ('Kolda', 7),
    ('Vélingara', 7),
    ('Médina Yoro Foulah', 7),

-- =====================
-- ZIGUINCHOR (8)
-- =====================
    ('Ziguinchor', 8),
    ('Bignona', 8),
    ('Oussouye', 8),

-- =====================
-- SÉDHIOU (9)
-- =====================
    ('Sédhiou', 9),
    ('Bounkiling', 9),
    ('Goudomp', 9),

-- =====================
-- TAMBACOUNDA (10)
-- =====================
    ('Tambacounda', 10),
    ('Bakel', 10),
    ('Goudiry', 10),
    ('Koumpentoum', 10),

-- =====================
-- MATAM (11)
-- =====================
    ('Matam', 11),
    ('Kanel', 11),
    ('Ranérou Ferlo', 11),

-- =====================
-- LOUGA (12)
-- =====================
    ('Louga', 12),
    ('Kébémer', 12),
    ('Linguère', 12),

-- =====================
-- KAFFRINE (13)
-- =====================
    ('Kaffrine', 13),
    ('Birkilane', 13),
    ('Malem Hodar', 13),
    ('Koungheul', 13),

-- =====================
-- KÉDOUGOU (14)
-- =====================
    ('Kédougou', 14),
    ('Salémata', 14),
    ('Saraya', 14);




INSERT INTO commune (nom, departement_id) VALUES

-- =====================
-- DAKAR
-- =====================
('Dakar-Plateau', 1),
('Medina', 1),
('Fann-Point E', 1),
('Grand Dakar', 1),

('Guédiawaye', 2),
('Sam Notaire', 2),
('Wakhinane Nimzatt', 2),
('Ndiarème Limamoulaye', 2),

('Pikine Nord', 3),
('Pikine Est', 3),
('Pikine Ouest', 3),
('Thiaroye', 3),

('Rufisque', 4),
('Bargny', 4),
('Sébikotane', 4),
('Diamniadio', 4),

-- =====================
-- THIÈS
-- =====================
('Thiès Nord', 5),
('Thiès Sud', 5),
('Thiès Est', 5),

('Mbour', 6),
('Saly Portudal', 6),
('Joal-Fadiouth', 6),

('Tivaouane', 7),
('Pambal', 7),
('Mékhé', 7),

-- =====================
-- SAINT-LOUIS
-- =====================
('Saint-Louis', 8),
('Gandon', 8),

('Richard-Toll', 9),
('Ross-Béthio', 9),

('Podor', 10),
('Ndioum', 10),

-- =====================
-- DIOURBEL
-- =====================
('Diourbel', 11),
('Ndoulo', 11),

('Bambey', 12),
('Ngoye', 12),

('Mbacké', 13),
('Touba', 13),

-- =====================
-- KAOLACK
-- =====================
('Kaolack', 14),
('Kahone', 14),

('Guinguinéo', 15),
('Mbadakhoune', 15),

('Nioro du Rip', 16),
('Paoskoto', 16),

-- =====================
-- FATICK
-- =====================
('Fatick', 17),
('Fimela', 17),

('Foundiougne', 18),
('Sokone', 18),

('Gossas', 19),
('Colobane', 19),

-- =====================
-- KOLDA
-- =====================
('Kolda', 20),
('Médina Chérif', 20),

('Vélingara', 21),
('Kounkané', 21),

('Médina Yoro Foulah', 22),
('Badion', 22),

-- =====================
-- ZIGUINCHOR
-- =====================
('Ziguinchor', 23),
('Niaguis', 23),

('Bignona', 24),
('Thionck Essyl', 24),

('Oussouye', 25),
('Mlomp', 25),

-- =====================
-- SÉDHIOU
-- =====================
('Sédhiou', 26),
('Marsassoum', 26),

('Bounkiling', 27),
('Madina Wandifa', 27),

('Goudomp', 28),
('Samine', 28),

-- =====================
-- TAMBACOUNDA
-- =====================
('Tambacounda', 29),
('Koussanar', 29),

('Bakel', 30),
('Diawara', 30),

('Goudiry', 31),
('Sinthiou Malème', 31),

('Koumpentoum', 32),
('Malem Niani', 32),

-- =====================
-- MATAM
-- =====================
('Matam', 33),
('Ogo', 33),

('Kanel', 34),
('Dembancané', 34),

('Ranérou Ferlo', 35),
('Lougré Thioly', 35),

-- =====================
-- LOUGA
-- =====================
('Louga', 36),
('Coki', 36),

('Kébémer', 37),
('Darou Mousty', 37),

('Linguère', 38),
('Barkédji', 38),

-- =====================
-- KAFFRINE
-- =====================
('Kaffrine', 39),
('Nganda', 39),

('Birkilane', 40),
('Diamagadio', 40),

('Malem Hodar', 41),
('Ndiognick', 41),

('Koungheul', 42),
('Ida Mouride', 42),

-- =====================
-- KÉDOUGOU
-- =====================
('Kédougou', 43),
('Nénéfécha', 43),

('Salémata', 44),
('Dakatéli', 44),

('Saraya', 45),
('Khossanto', 45);


INSERT INTO adresses (region, departement, commune) VALUES


-- DAKAR

('Dakar', 'Dakar', 'Plateau'),
('Dakar', 'Dakar', 'Médina'),
('Dakar', 'Dakar', 'Grand Dakar'),
('Dakar', 'Dakar', 'Parcelles Assainies'),

('Dakar', 'Guédiawaye', 'Golf Sud'),
('Dakar', 'Guédiawaye', 'Sam Notaire'),
('Dakar', 'Guédiawaye', 'Ndiarème Limamoulaye'),

('Dakar', 'Pikine', 'Pikine Ouest'),
('Dakar', 'Pikine', 'Thiaroye'),
('Dakar', 'Pikine', 'Yeumbeul'),

('Dakar', 'Rufisque', 'Rufisque Est'),
('Dakar', 'Rufisque', 'Bargny'),
('Dakar', 'Rufisque', 'Diamniadio'),


-- THIÈS

('Thiès', 'Thiès', 'Thiès Nord'),
('Thiès', 'Thiès', 'Thiès Est'),
('Thiès', 'Thiès', 'Thiès Ouest'),

('Thiès', 'Mbour', 'Saly'),
('Thiès', 'Mbour', 'Joal-Fadiouth'),
('Thiès', 'Mbour', 'Ngaparou'),

('Thiès', 'Tivaouane', 'Mékhé'),
('Thiès', 'Tivaouane', 'Pambal'),


-- SAINT-LOUIS

('Saint-Louis', 'Saint-Louis', 'Sor'),
('Saint-Louis', 'Saint-Louis', 'Pikine'),
('Saint-Louis', 'Dagana', 'Richard-Toll'),
('Saint-Louis', 'Podor', 'Podor');


INSERT INTO hopitaux (id, nom, latitude, longitude, type_etablissement)
VALUES
    ('9a4cb280-2ccf-4d97-81aa-bf38bf507a50', 'Hôpital Militaire de Ouakam', 14.7150394, -17.4814547, 'PUBLIC'),
    ('09a98a61-3053-467a-baed-57d3b2619eea', 'Centre de Santé de Diofior', 14.1788199, -16.6604301, 'PUBLIC'),
    ('ee099eb4-d177-491c-bb31-010cb16899a4', 'Hôpital de la Paix', 12.5688416, -16.2763924, 'PUBLIC'),
    ('7e392e8a-fba5-4e57-a065-deb22930563c', 'Maternity Clinic', 12.4561971, -16.6357799, 'PUBLIC'),
    ('9b16a6b7-f5de-4c7a-a23c-c86e5359e9ad', 'Hôpital Roi Baudouin', 14.7743154, -17.3844039, 'PUBLIC'),
    ('d21ecff9-24d4-4ba1-a05b-b31a16fbbd29', 'Centre Hospitalier Youssou Mbargane Diop', 14.7262154, -17.2597138, 'PUBLIC'),
    ('1dd7f006-158e-455d-a51a-142c37abbf0f', 'Centre Hospitalier Régional "Heinrich Lübke"', 14.6426353, -16.2318536, 'PUBLIC'),
    ('febb3f9d-1e20-498f-8e56-4086013d6850', 'Hôpital Général de Grand Yoff', 14.7318249, -17.4448433, 'PUBLIC'),
    ('faf4b396-9f58-4ea3-bbc0-c056a02f0cc0', 'Clinique Fann Hock;SOS Cardio', 14.6799027, -17.4620348, 'PUBLIC'),
    ('abc65af7-80be-4fde-8373-aa5c5cbe4658', 'CCBM electonique', 16.026429, -16.5046454, 'PUBLIC'),
    ('0adaeedf-368d-46c6-a64d-a91ff10a95f8', 'Centre Hospitalier de Bignona', 12.8095688, -16.231975, 'PUBLIC'),
    ('3ecf04af-28e7-4145-b438-252bb8c1a2ee', 'Centre de santé de Grand Dakar', 14.7059606, -17.4540171, 'PUBLIC'),
    ('d90dfca6-8d5b-4949-930b-eb209e24257c', 'Centre médical de Garnison Nord', 16.0346522, -16.5032257, 'PUBLIC'),
    ('24118081-194d-4249-8594-246c6386837b', 'District Sanitaire de Kédougou', 12.5561978, -12.1835118, 'PUBLIC'),
    ('1d2d1c49-8a04-4cef-a06f-4f0db118cd4b', 'centre de santé Polyclinique de Rufisque', 14.7118924, -17.2717612, 'PUBLIC'),
    ('5f2289b3-166d-4fe6-b9ce-d4c261f3c91a', 'Hopital Regional de Ziginchor', 12.5580293, -16.2823823, 'PUBLIC'),
    ('63a2541f-cfde-40f4-bcfb-5c1e46ec08e4', 'Hopital Saint Jean de Dieu', 14.8018023, -16.9357507, 'PUBLIC'),
    ('8d4a4d55-9dd9-4d68-bda5-614b45c94a61', 'Hôpital des Enfants de Diamniadio', 14.7272055, -17.1641207, 'PUBLIC'),
    ('391d838d-e1af-46c9-bb97-6a3cf0a83359', 'Dallal Jamm', 14.7728688, -17.4097894, 'PUBLIC'),
    ('f5b83b87-3118-4f14-aa44-9865b04626b5', 'Hopital Barthimée', 14.7749213, -16.9384758, 'PUBLIC'),
    ('4a9aa4d2-6214-4193-a7d3-693e8762f653', 'Centre de Santé de Kébémer', 15.3716675, -16.4425104, 'PUBLIC'),
    ('01444d2c-96d5-4b4d-ba95-22dd7666d3ec', 'Centre hospitalier régional Amadou Sakhir Mbaye Louga', 15.6165278, -16.236426, 'PUBLIC'),
    ('7df9b017-8760-4862-a9ea-f959f83c4f52', 'SAMU Municipal', 14.7386473, -17.4655884, 'PUBLIC'),
    ('2c325b24-0d83-47e1-9f9c-0bf62e775930', 'Centre hospitalier de Hann', 14.7249916, -17.4294169, 'PUBLIC'),
    ('123012e9-871e-4b82-8f93-82b768c7cc64', 'Centre de santé El Massamba Sall', 14.9535502, -16.8196879, 'PUBLIC'),
    ('b1982bab-9bcc-465e-af4a-6eb920652429', 'Hopital Abdoul Aziz Sy Dabakh', 14.9466282, -16.8070808, 'PUBLIC'),
    ('20cfb188-d032-4e8d-9016-23afa5a29876', 'Centre de Santé Abdou Aziz Sy Dabakh', 14.757443, -17.4385229, 'PUBLIC'),
    ('b86fdf20-4008-4797-87b0-1fdcc7d9dcbb', 'Centre de Santé de Sokone', 13.8787905, -16.3688407, 'PUBLIC'),
    ('c8242a37-9808-4f71-a281-61452bd9b90c', 'Hopital de Keur Massar', 14.7780164, -17.3221023, 'PUBLIC'),
    ('bf1c7ee0-ab3a-4753-afe7-ee39a6ca12cb', 'Keur Soeurs', 14.7728904, -17.394816, 'PUBLIC'),
    ('912bc64a-2989-4517-b7aa-5a8efa8a54c1', 'Hopital Principal de Dakar', 14.6612822, -17.434785, 'PUBLIC'),
    ('2a7447e9-1431-4268-9455-7959a0ff2181', 'Hopital El Hadj Ibrahima Niass', 14.14063, -16.0755925, 'PUBLIC'),
    ('13c58c69-bac7-43aa-862c-9b1e843de6f0', 'Hôpital régional de Thiés', 14.7811294, -16.9233259, 'PUBLIC'),
    ('c34e8706-c652-4a26-a52d-37ad580a53e2', 'Hopitale Régionale de Ndioum', 16.5091414, -14.6539427, 'PUBLIC'),
    ('9801b064-34a2-47d7-8e16-11145637c0e0', 'Grande Mosquée de Touba', 14.8632007, -15.8755582, 'PUBLIC'),
    ('eec0ad47-a3b9-4aea-8440-889830e6caa3', 'Hôpital de Keur Moussa Frontière', 13.6228855, -15.8661853, 'PUBLIC'),
    ('f1cf1c52-b617-4f97-ab87-a3a1180fbc51', 'Hopital de Thiaroye', 14.7542902, -17.3761243, 'PUBLIC'),
    ('9758418f-cf2e-4bf5-964d-17dd1935f464', 'Centre Hospitalier National Mathlaboul Fawzaini de Touba', 14.8582741, -15.9088385, 'PUBLIC'),
    ('22bf781a-3718-4ccb-a5ec-29d776e27291', 'Hôpital Aristide Le Dantec', 14.6571018, -17.4366947, 'PUBLIC'),
    ('5542396a-581c-4fa3-a6d2-6fa853ac9276', 'Hôpital Magatte Lo', 15.4006907, -15.1120264, 'PUBLIC'),
    ('c860a3aa-d170-4279-aa82-a8675a3a74d7', 'Centre de santé de Tambacounda', 13.7788515, -13.6736563, 'PUBLIC'),
    ('7a214d38-184e-4276-87a4-ecace25f8ea7', 'Poste de santé Unité 22', 14.7511854, -17.4451774, 'PUBLIC'),
    ('717c59b2-34fe-4376-a837-792df6ef049d', 'Hopital Médine', 14.9430532, -16.8136647, 'PUBLIC'),
    ('a5b1c9db-f297-4afd-a563-8afb4dcc9be0', 'Centre medico-social de la fonction publique', 14.6656937, -17.4348137, 'PUBLIC'),
    ('3458cbfa-0421-40b3-8844-900da289adba', 'Ourossogui Hospital', 15.6039078, -13.3301484, 'PUBLIC'),
    ('a71b3380-59ea-43ba-a3da-eb7340d40e4e', 'Hopital de Mbacké', 14.7989743, -15.9099361, 'PUBLIC'),
    ('388febd0-2864-459e-b48f-df2ee8d09e20', 'Hopital Phillipe.M.Senghor', 14.7560788, -17.4736346, 'PUBLIC'),
    ('54236e20-fefb-4b34-be8e-63020950f19b', 'Hopital Elisabeth Diouf de Diamniadio', 14.7190416, -17.1835897, 'PUBLIC'),
    ('16dd2686-80de-4e9f-b0f7-b9fa2ad881f5', 'Hôpital Départemental de Mbour', 14.4229364, -16.9872102, 'PUBLIC'),
    ('cf194332-8d3b-4a03-b663-f6d2896814dc', 'Sheikh Khalifa Bin Hamad Al-Thani', 14.7757931, -17.3556472, 'PUBLIC'),
    ('b9df6630-d593-4403-bdde-32cabd099f27', 'Cabinet Dentaire Akram;Cabinet Medical AFIA', 14.8593322, -15.8869674, 'PUBLIC'),
    ('45fe8362-d3fd-41cd-9660-90f390d022a2', 'Temporary Dar Salaam Health Post', 12.6287832, -12.7899421, 'PUBLIC'),
    ('cd5a2b57-dbcd-451b-b0c3-45c5e48f3614', 'District Health Center', 12.8338866, -11.7700093, 'PUBLIC'),
    ('dc1aa90e-d5a2-4d41-99d3-4b65911d1358', 'Hôpital Régional de Tambacounda', 13.7532785, -13.6726093, 'PUBLIC'),
    ('8bc89a7c-3e04-485a-ae93-025e2f59b59d', 'Centre médical Garnison Saint-Louis', 16.034427, -16.5031953, 'PUBLIC'),
    ('bd881340-51a2-435d-8848-79dfb851caa9', 'Centre médical Garnison Saint-Louis', 16.0343142, -16.5033545, 'PUBLIC'),
    ('e2ed6c40-2dff-4f72-9179-0ad2e4a40220', 'District sanitaire de Saint-Louis Qrt Hydrobase', 16.0038302, -16.5094887, 'PUBLIC'),
    ('a71664d4-096b-4312-8c24-372ee46d936e', 'Plateau médical Fama', 13.7625712, -13.6697476, 'PUBLIC'),
    ('774de364-d534-43f6-9220-855b1bb116f1', 'poste de santé municipal 1', 14.7480876, -17.3959069, 'PUBLIC'),
    ('05445b3b-db3a-484a-9cc3-5f00accc9d02', 'Poste de santé Croix rouge', 14.7516924, -17.3920028, 'PUBLIC'),
    ('d101bb95-8853-417b-ac0a-ccc5657562b9', 'Clinique Milogo', 13.7622313, -13.6672623, 'PUBLIC'),
    ('88cd6925-eb80-484d-9fa9-2ab258f36d58', 'Poste de Santé de Kabendou', 12.914527, -14.1179784, 'PUBLIC'),
    ('2c7aaf2e-8911-4970-8d9d-d80d06e66082', 'BARTHEMY', 14.7759673, -16.9356561, 'PUBLIC'),
    ('e997249f-160d-4500-bdbb-c4ef1b68b3d1', 'Clinique du Littoral', 14.4519186, -17.0391584, 'PUBLIC'),
    ('3f365ea4-b9e0-4218-9ea3-e15aebdab992', 'Poste de santé Toubacouta', 13.7865551, -16.4741945, 'PUBLIC'),
    ('dda8b63a-6147-43a2-af3c-12c819224b07', 'Poste de sante Guinaw rail Sud', 14.7490896, -17.3916198, 'PUBLIC'),
    ('af10ef45-266f-4e92-be43-653ee199c9fb', 'Maternité de Grand-Yoff;Poste de santé Grand-Yoff 2', 14.7363415, -17.4511452, 'PUBLIC'),
    ('e3d3309d-8e81-4abe-98ba-295772abf883', 'Institut Pasteur', 14.7457415, -17.4669308, 'PUBLIC'),
    ('3a379cab-a497-4827-8fb9-69a1273910d6', 'Hôpital régional de Saint-Louis', 16.0228415, -16.505971, 'PUBLIC'),
    ('c56364b0-8794-4e79-8bf9-75618ecf21f1', 'District Sanitaire Wakhinane Nimzatt II', 14.775967, -17.3717097, 'PUBLIC'),
    ('66313ddb-74d1-4e33-9a2d-b2f2a6c137a0', 'Polyclinique de la Médina', 14.6781053, -17.4456371, 'PUBLIC'),
    ('30c9c4b6-9e73-4493-adc7-f3c894564a09', 'Centre Medico Sportif Fabrique D''Espoir', 14.786038, -17.3667356, 'PUBLIC'),
    ('80eede61-d216-487e-bea3-24a7874e1b33', 'Infirmerie Militaire de Kédougou', 12.5546804, -12.1890091, 'PUBLIC'),
    ('e05fe644-4994-4466-9a14-7432660fc04b', 'Hopital d''Elinkine', 12.5081238, -16.6626325, 'PUBLIC'),
    ('11f5d1bb-3d53-4624-851b-b80d778ef906', 'Centre de Premiers Secours', 13.376638, -13.3764664, 'PUBLIC'),
    ('b579741a-9c5c-4429-a747-4fe73e785636', 'Polyclinique Louis Pasteur ''''La Référence''''', 14.711646, -17.270406, 'PUBLIC'),
    ('aff957b5-3d1c-480d-8899-0dd25bbf96f9', 'Centre de Santé de Dodji', 15.5357517, -14.9504461, 'PUBLIC'),
    ('672ce358-2b95-4764-8280-1fbe91b30f64', 'Clinique Suma Assistance', 14.7104835, -17.4698793, 'PUBLIC'),
    ('f5535eca-0d4c-470d-b958-571abd55110a', 'Maternité de Badiate-Grand', 12.5212405, -16.392519, 'PUBLIC'),
    ('189ac36a-fecc-47a0-acb6-d8e8e2d4bde6', 'Hopital Regional de Kolda', 12.8852457, -14.9190926, 'PUBLIC'),
    ('d4a7e5d6-a789-450a-996e-ae48514d8e6d', 'Institut de Prevoyance Retraite du Senegal Centre Medico-Social de Pikine', 14.7480784, -17.4065897, 'PUBLIC'),
    ('768feca4-8c71-437b-b224-4bd512758506', 'Poste de sante Catholique Notre Dame du Cap-Vert', 14.7482668, -17.4018889, 'PUBLIC'),
    ('463f490c-3a68-4daf-a420-d84accf98911', 'Hopital psychiatrique de thiaroye', 14.7430276, -17.3539093, 'PUBLIC'),
    ('6860c6b5-92b6-4b08-8e8e-107603902945', 'Maternité Dalifort Foirail', 14.7405648, -17.4176162, 'PUBLIC'),
    ('39b7b555-cb33-4e90-a236-ab70dc903822', 'Poste de Santé Sourah', 14.8695485, -15.864556, 'PUBLIC'),
    ('269c4660-bb91-4154-8b1f-358d4bdf77ac', 'Poste de santé de Bambilor', 14.8011082, -17.1860098, 'PUBLIC'),
    ('2f6c0684-c912-4423-ac9d-af8663e0428a', 'La Croix Bleue', 14.7228698, -17.4512597, 'PUBLIC'),
    ('b31cad1c-2df1-40b8-91bf-84b68bdbd7da', 'Poste de santé', 12.8351792, -15.9771522, 'PUBLIC'),
    ('86b07756-054b-4755-ad69-2538aaef4270', 'Poste de santé Serigne Mouhamadou Moustapha Mbacke', 14.8750603, -15.8824056, 'PUBLIC'),
    ('d821bfc7-5f20-4ae6-8c7a-bfbc86238337', 'Hopital COUD', 14.6898514, -17.4631544, 'PUBLIC'),
    ('8c089bb6-2425-4d60-8102-1986fd333272', 'Institut de Pediatrie Social IPS Mouhamadou Fall', 14.7687976, -17.3888619, 'PUBLIC'),
    ('507340d9-69a1-4560-8f73-8493dfdd3bac', 'Case de Santé Saré Dickel', 13.0376313, -14.6401756, 'PUBLIC'),
    ('9d532b3d-6546-4599-8e5f-133c21ae444b', 'Centre de santé darou salam', 14.8117292, -15.8921013, 'PUBLIC'),
    ('81f8e2bc-3a82-452b-9ccf-bb1d4c8ba9c5', 'Centre hospitalier national pour enfant Albert Royer', 14.6922308, -17.4649264, 'PUBLIC'),
    ('77d3f755-5dec-4a0f-8fe7-90852f090a49', 'Hopital de Fann', 14.6931968, -17.4666171, 'PUBLIC'),
    ('26fd5fb1-6c20-461b-b575-33015c149848', 'EPS (Etablissement Publique de Santé) niveau 1 de Kaffrine', 14.0989168, -15.5569385, 'PUBLIC'),
    ('f7bab791-91a8-4eec-828f-4c6996a340d0', 'Hôpital Régional Thierno Birahim Ndao', 14.1045223, -15.5267575, 'PUBLIC'),
    ('a50cf0a9-6bb2-4364-8cfe-bee44ad371c2', 'Hôpital de Ninéfécha', 12.5598738, -12.4767377, 'PUBLIC'),
    ('1559c716-cb5c-401a-822d-cd5246625dac', 'poste de sante', 14.4429838, -16.986058, 'PUBLIC'),
    ('d873f7b2-8552-4ac7-8926-84ece19c4155', 'Maternité Muriel Africa', 14.4352219, -16.9798044, 'PUBLIC'),
    ('7272cc6c-6a2a-4627-bc95-7b1618ef8b9d', 'Centre de Santé de Grand Mbour', 14.4350352, -16.9797904, 'PUBLIC'),
    ('199398e9-fc0d-4f4c-8e9c-42a6bfc74835', 'Pharmacie LES PARAS', 14.7552055, -17.3703306, 'PUBLIC'),
    ('07105462-29e1-4522-9818-9fac750f6741', 'TERRAIN DE JEU', 16.0047351, -16.4912894, 'PUBLIC'),
    ('2bf6973e-eb5b-4edd-b9e6-68d197651216', 'HP', 14.7438966, -17.2800284, 'PUBLIC'),
    ('0157aa1f-be02-42c6-84ca-9020eee05a8d', 'Centre de Santé', 14.0786729, -16.1587795, 'PUBLIC'),
    ('2cb8be28-8a73-46c7-a10f-a740131528d6', 'Poste de Santé Ourour', 14.3271608, -16.0309017, 'PUBLIC'),
    ('856302f7-e83e-4f66-abcc-23060077dfb5', 'Centre médico-social Keru Yakaar', 14.7385333, -17.4570535, 'PUBLIC'),
    ('be82767a-8040-498c-bf90-4e2b97cf2989', 'District sanitaire Cite Gendarmerie', 14.7631341, -17.2805393, 'PUBLIC'),
    ('0939427c-2a2b-4ce3-95e3-346935fde36b', 'case de santé', 14.4031265, -16.7941855, 'PUBLIC'),
    ('079a9db1-42ac-40d7-8eaa-dc8a57060b03', 'District Sanitaire de Godomp', 12.5666235, -15.8823577, 'PUBLIC'),
    ('c4e4710b-bd73-47df-b7e3-945d850e49b4', 'Centre de Santé de Malicounda Ngoukhoudj', 14.4573487, -16.9539596, 'PUBLIC'),
    ('e5bc9db7-bd46-4104-ad04-a4542dc6f897', 'Poste de santé koumbal', 14.0172295, -16.0178336, 'PUBLIC'),
    ('c29dba21-23a0-49d0-ac12-bdbfd175dd64', 'Imagerie medicale', 14.7026955, -17.4726389, 'PUBLIC'),
    ('8b31132f-1311-4dec-9c1b-d3520a13f648', 'Wari', 14.7028627, -17.472474, 'PUBLIC'),
    ('78051767-037e-43a7-8a01-adfdfb3784cd', 'Centre de Santé de Kolda (Sikilo)', 12.8986076, -14.9381684, 'PUBLIC'),
    ('6a7a6ded-cefb-4121-a654-e0fd3f5d5df7', 'Service Medico social des Etudiants CROUS', 16.0667319, -16.4265564, 'PUBLIC'),
    ('01fcdbb1-9394-4cea-9e8f-2d42f5ecbf08', 'centre de sante amadou malick guaye', 16.6427296, -14.9534247, 'PUBLIC'),
    ('4dea9c32-3f9f-4df9-ad50-64658cda7bed', 'District Sanitaire de Thionck-Essyl', 12.7937854, -16.5096061, 'PUBLIC'),
    ('7c40fe50-04fb-41b0-bc75-85cd687844d2', 'CERPAD (Centre de Recherche et de Prise en charge Ambulatoire de la Drépanocytose)', 16.0493031, -16.4332574, 'PUBLIC'),
    ('4256ebcf-182a-46d3-a392-9be3d5c00794', 'Dr Alakpo', 14.7545003, -17.4624214, 'PUBLIC'),
    ('176002d4-a49c-40c1-b03d-0c658bb7d1fd', 'Naby Touré Frères', 14.7674584, -17.4131898, 'PUBLIC'),
    ('b91a55f0-bda6-468b-a58f-ef2773bcb099', 'Hôpital Militaire de Ouakam', 14.7151889, -17.4816358, 'PUBLIC'),
    ('e5b924ec-a4ff-483d-a105-66ff54a81c86', 'Touba Laboratoire d''analyses Medicales', 14.8585207, -15.8780197, 'PUBLIC'),
    ('6321ec2f-1ae0-4d2d-b9bc-270cc54d4031', 'Complexe Sanitaire Cheikh Ah. Bamba', 14.8572669, -15.8946407, 'PUBLIC'),
    ('fef7dd55-3058-4df5-87a5-4df387b6a675', 'Poste de santé pikine sor', 15.9989193, -16.4954058, 'PUBLIC'),
    ('8e6ec50f-fa16-4021-844a-23085e4f77f8', 'Centre de santé de Saint Louis', 16.0111015, -16.4933452, 'PUBLIC'),
    ('0b2d3aa7-5c15-43b0-99cb-cf2bee5ce02f', 'Poste de santé de khor', 16.0331234, -16.4715159, 'PUBLIC'),
    ('1fe2785d-1369-4812-aec4-bb132e23e954', '#cognizantindia', 12.5696414, -16.2772125, 'PUBLIC'),
    ('fb4e83e5-1ff4-4547-a0a7-6fabc68afe4a', 'Centre de Santé de Nioro du Rip', 13.7500692, -15.7755584, 'PUBLIC'),
    ('8e7dcdd7-1cd3-44fb-a54c-7e6975e9cfcf', 'centre hospitalier national mathlaboul fawzaini de touba', 14.8583869, -15.9087225, 'PUBLIC'),
    ('c81406c1-20c0-4c39-9b06-03fa63520985', 'District Sanitaire de Sédhiou', 12.7071327, -15.5611365, 'PUBLIC'),
    ('282c723d-a2d8-4e80-8620-4d9a5a6360ae', 'district sanitaire Fiacre Coly de Khombole', 14.760766, -16.683435, 'PUBLIC'),
    ('3f4ef228-a4b2-4cb4-9984-3040d3264afc', 'District sanitaire de Mékhé', 15.1079614, -16.6271909, 'PUBLIC'),
    ('36a22d52-34a2-4b9c-b2bd-7645e8407bd7', 'Centre de Santé de Gossas', 14.4918607, -16.0663037, 'PUBLIC'),
    ('8adf4190-3784-4c25-93b4-b78f852b131f', 'Centre de Santé de Koungheul', 13.981676, -14.7919726, 'PUBLIC'),
    ('2c8f3af6-0579-4941-b3ad-88d1192e5121', 'Centre Hospitalier Régional de Kolda', 12.9054789, -14.9484396, 'PUBLIC'),
    ('1bf17d70-b22c-4825-97e2-198db6456bee', 'Poste de Santé', 16.5227749, -15.2237618, 'PUBLIC'),
    ('3c439bee-8fd8-454f-b4bb-91537c1a0507', 'Poste de Santé de BOTOU', 13.8047815, -13.5824065, 'PUBLIC'),
    ('f9f11780-cc9c-4e70-87e3-583820f853eb', 'Poste de Santé  Saré Kemo', 12.8800253, -14.9557234, 'PUBLIC'),
    ('d258dcab-3663-4e31-acc6-4bac133d4c27', 'District Sanitaire de Linguère', 15.397049, -15.1141922, 'PUBLIC'),
    ('1a04b32d-7db4-4e5b-a385-c03bcbdf4c7a', 'établissement public de sante de richard toll', 16.4688218, -15.6891342, 'PUBLIC'),
    ('5e51d083-ca1b-46a4-990b-59a09041f8d4', 'poste de sante pmi de sor', 16.0256068, -16.4979662, 'PUBLIC'),
    ('016bf96c-6b19-48c0-b86a-ea2295b9d917', 'Établissement public de sante de richard toll', 16.468625, -15.689494, 'PUBLIC'),
    ('75451436-65ce-40ef-82b4-adc182006a2e', 'centre de sante de mpal', 15.9191045, -16.2702672, 'PUBLIC'),
    ('aa704d85-62a8-479f-870c-78de6f9bc295', 'centre de sante de saint_louis', 16.010812, -16.4932212, 'PUBLIC'),
    ('8e07fedc-6da1-4100-8e62-550cc2061010', 'poste de sante mboyo', 16.6374678, -14.8015645, 'PUBLIC'),
    ('baef7ec1-4328-4722-ae25-d4493db3314d', 'centre de sante de dagana', 16.5107737, -15.5131268, 'PUBLIC'),
    ('b7bad6a6-9e6d-4e49-bb1d-2cddac3e1220', 'Tawfeikh lavage', 14.7751516, -17.3297759, 'PUBLIC'),
    ('047b4e5a-859d-4856-b6f6-c90fa93a95be', 'Multi service Serigne Babacar Sy', 14.7700767, -17.3350937, 'PUBLIC'),
    ('c0f0b47a-821c-48ef-82dd-f911d19cacf1', 'centre de sante de galoya', 16.0647989, -13.8609154, 'PUBLIC'),
    ('a63859a8-641b-4eaf-aa7d-dafaf628f15a', 'centre de sante de cas-cas', 16.3883854, -14.0716206, 'PUBLIC'),
    ('57322ced-0772-4b12-bd25-0e6343009caf', 'centre de sante aéré lao', 16.3803617, -14.3127759, 'PUBLIC'),
    ('52e3bcd8-d6a8-4263-9dc3-c733d1c0b9ea', 'centre de sante pété', 16.1016123, -13.954996, 'PUBLIC'),
    ('18bd4954-8955-4734-80f4-0274f4ac0f1c', 'PHARMACIE', 16.0609545, -13.8114641, 'PUBLIC'),
    ('536e0800-9bc7-40b0-b7de-481d5c2745d7', 'Maternité', 15.3506208, -16.3469011, 'PUBLIC'),
    ('73f8f05a-2f8d-47b4-822b-7f6a4af35d2f', 'Centre de sante de Thierno Mouhamadou Saïdou Ba', 15.3040447, -13.9590618, 'PUBLIC'),
    ('420b1b2f-fa4b-4791-91ed-01f694ade632', 'Hopital regionale de Matam', 15.6605149, -13.2618979, 'PUBLIC'),
    ('ab0e19df-1cab-47e6-9e0d-fa2cb414f34a', 'Case de Sante Ansde Balla', 16.0121958, -13.7097376, 'PUBLIC'),
    ('8bfb04c9-f442-434c-b7e2-fbc5da04beb6', 'Centre de sante de Thilogne', 15.9654639, -13.6065914, 'PUBLIC'),
    ('a41ac15f-dbc8-413f-963c-ef3a14bac013', 'Centre de Sante de Hamady Hounare', 15.3541181, -13.027545, 'PUBLIC'),
    ('b39763c4-ca89-4e50-99ad-c577aed9d99d', 'Centre Hospitalier Regional de Ourossogui', 15.6038666, -13.3298457, 'PUBLIC'),
    ('56b562fb-2357-4a3f-9eef-e6847c65532d', 'Hopital Abdoul Cisse Kane', 15.9877066, -13.6536696, 'PUBLIC'),
    ('7ca7dcfb-cdbf-40e3-899e-943b88e6f457', 'Centre de sante de reference de Kanel', 15.4854524, -13.185398, 'PUBLIC'),
    ('a33b5639-a3eb-41e7-bcea-39eafea4d394', 'Centre de sante Matam', 15.6535257, -13.2538692, 'PUBLIC'),
    ('7e1bf00b-d428-4554-b3b2-948309e0d0d5', 'Clinique Yama Dr MBODJ', 14.7544574, -17.4637853, 'PUBLIC'),
    ('8377b5fc-fe67-4f1b-8acc-ad99754d7197', 'District sanitaire', 13.9355947, -16.7632862, 'PUBLIC'),
    ('bc397c1c-4808-4af5-91ec-1162e63fc873', 'Centre de santé de Niodior', 13.8571521, -16.7228895, 'PUBLIC'),
    ('3f3cfc2f-7925-4ffb-969c-bafcd3741e84', 'Case de santé de Falia', 13.9189278, -16.679159, 'PUBLIC'),
    ('8242d02e-2e50-4a8f-b9e0-a42e2e704164', 'Centre de Santé', 15.8421628, -16.2349145, 'PUBLIC'),
    ('d74e36ae-7c5d-4e9b-b0a4-cf3a2551df62', 'Case de santé Nianghe', 12.6068466, -12.3209068, 'PUBLIC'),
    ('515459c1-51c2-451b-a11b-a5f4826e8383', 'Hopital de fann', 14.6932181, -17.4665714, 'PUBLIC'),
    ('e40ec054-5428-47a4-8489-7ef7fac5bfc3', 'poste de sante pointe sarene', 14.2860353, -16.9189218, 'PUBLIC');