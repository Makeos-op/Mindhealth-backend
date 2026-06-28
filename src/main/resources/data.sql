INSERT INTO roles (name) VALUES ('ROLE_PACIENTE') ON CONFLICT (name) DO NOTHING;
INSERT INTO roles (name) VALUES ('ROLE_PROFESIONAL') ON CONFLICT (name) DO NOTHING;
INSERT INTO roles (name) VALUES ('ROLE_ADMIN') ON CONFLICT (name) DO NOTHING;

INSERT INTO usuario (nombre, correo, contrasena, edad, genero, fecha_registro, activo, calendario_vinculado, notificar_red_apoyo, estilo_lenguaje_ia)
VALUES 
('Administrador', 'admin@mindhealth.com', '$2a$12$svQPBsyZRf3r1gyer6SoteyP3Y6YNfXX6M29ikivAeq/2wcNEJsRS', 30, 'Masculino', CURRENT_DATE, true, false, false, 'INFORMAL'),
('Dr. Carlos Mendoza', 'carlos.mendoza@mindhealth.com', '$2a$12$TFLvcCMeUK2rh7zVwFcB6uWFT2cI6xAsX.dpbbF1XO8TEQxlT/Lne', 42, 'Masculino', CURRENT_DATE, true, false, false, 'INFORMAL'),
('Dra. Elena Rossi', 'elena.rossi@mindhealth.com', '$2a$12$TFLvcCMeUK2rh7zVwFcB6uWFT2cI6xAsX.dpbbF1XO8TEQxlT/Lne', 38, 'Femenino', CURRENT_DATE, true, false, false, 'INFORMAL')
ON CONFLICT (correo) DO NOTHING;

INSERT INTO usuario_rol (id_usuario, id_rol)
VALUES 
((SELECT id_usuario FROM usuario WHERE correo = 'admin@mindhealth.com'), (SELECT id_rol FROM roles WHERE name = 'ROLE_ADMIN')),
((SELECT id_usuario FROM usuario WHERE correo = 'carlos.mendoza@mindhealth.com'), (SELECT id_rol FROM roles WHERE name = 'ROLE_PROFESIONAL')),
((SELECT id_usuario FROM usuario WHERE correo = 'elena.rossi@mindhealth.com'), (SELECT id_rol FROM roles WHERE name = 'ROLE_PROFESIONAL'))
ON CONFLICT DO NOTHING;

INSERT INTO profesional (nombre, especialidad, disponible, id_usuario)
VALUES 
('Dr. Carlos Mendoza', 'Psicología de la Emergencia', true, (SELECT id_usuario FROM usuario WHERE correo = 'carlos.mendoza@mindhealth.com')),
('Dra. Elena Rossi', 'Terapia Cognitivo Conductual', true, (SELECT id_usuario FROM usuario WHERE correo = 'elena.rossi@mindhealth.com'))
ON CONFLICT DO NOTHING;