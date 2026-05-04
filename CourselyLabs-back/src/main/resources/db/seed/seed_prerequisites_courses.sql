INSERT INTO course_prerequisites (course_id, prerequisite_course_id, completion_threshold)
SELECT c.id, p.id, 80
FROM courses c
JOIN courses p ON p.slug = 'introduccion-postgresql'
WHERE c.slug = 'vue3-desarrollo-web';

INSERT INTO course_prerequisites (course_id, prerequisite_course_id, completion_threshold)
SELECT c.id, p.id, 85
FROM courses c
JOIN courses p ON p.slug = 'python-principiantes'
WHERE c.slug = 'algoritmos-estructuras-datos';

INSERT INTO course_prerequisites (course_id, prerequisite_course_id, completion_threshold)
SELECT c.id, p.id, 70
FROM courses c
JOIN courses p ON p.slug = 'python-principiantes'
WHERE c.slug = 'diseno-ui-ux';

INSERT INTO course_prerequisites (course_id, prerequisite_course_id, completion_threshold)
SELECT c.id, p.id, 80
FROM courses c
JOIN courses p ON p.slug = 'diseno-ui-ux'
WHERE c.slug = 'marketing-digital-negocios';
