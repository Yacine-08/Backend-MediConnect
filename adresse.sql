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
('Saint-Louis', 'Podor', 'Podor')
ON CONFLICT DO NOTHING;
