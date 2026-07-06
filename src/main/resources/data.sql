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

INSERT INTO contenido_terapeutico (titulo, tipo, url_recurso, emocion_asociada)
SELECT * FROM (VALUES
    ('Respiración 4-7-8 para calmar la ansiedad', 'EJERCICIO', 'https://www.youtube.com/watch?v=EjemploRespiracion', 'Ansiedad'),
    ('Meditación guiada de 10 minutos', 'VIDEO', 'https://www.youtube.com/watch?v=EjemploMeditacion', 'Ansiedad'),
    ('Cómo reconocer los síntomas de la ansiedad', 'ARTICULO', 'https://mindhealth.example.com/articulos/ansiedad-sintomas', 'Ansiedad'),
    ('Cuestionario de autoevaluación de ansiedad', 'RECURSO_INTERACTIVO', 'https://mindhealth.example.com/interactivo/autoeval-ansiedad', 'Ansiedad'),
    ('Rutina de relajación muscular progresiva', 'EJERCICIO', 'https://www.youtube.com/watch?v=EjemploRelajacion', 'Estrés'),
    ('Manejo del estrés en el trabajo', 'ARTICULO', 'https://mindhealth.example.com/articulos/estres-laboral', 'Estrés'),
    ('Video: técnicas rápidas anti-estrés', 'VIDEO', 'https://www.youtube.com/watch?v=EjemploEstres', 'Estrés'),
    ('Higiene del sueño: guía práctica', 'ARTICULO', 'https://mindhealth.example.com/articulos/higiene-sueno', 'Insomnio'),
    ('Ejercicio de relajación para dormir mejor', 'EJERCICIO', 'https://www.youtube.com/watch?v=EjemploInsomnio', 'Insomnio'),
    ('Sonidos relajantes para conciliar el sueño', 'VIDEO', 'https://www.youtube.com/watch?v=EjemploSonidos', 'Insomnio'),
    ('Journaling para procesar la tristeza', 'ARTICULO', 'https://mindhealth.example.com/articulos/journaling-tristeza', 'Tristeza'),
    ('Video: cómo acompañar tus emociones', 'VIDEO', 'https://www.youtube.com/watch?v=EjemploTristeza', 'Tristeza'),
    ('Ejercicio de gratitud diaria', 'RECURSO_INTERACTIVO', 'https://mindhealth.example.com/interactivo/gratitud', 'Calma'),
    ('Mindfulness para mantener la calma', 'VIDEO', 'https://www.youtube.com/watch?v=EjemploCalma', 'Calma'),
    ('Artículo: beneficios de la calma mental', 'ARTICULO', 'https://mindhealth.example.com/articulos/beneficios-calma', 'Calma')
) AS nuevo(titulo, tipo, url_recurso, emocion_asociada)
WHERE NOT EXISTS (SELECT 1 FROM contenido_terapeutico);