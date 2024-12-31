-- Crear Alimentos con información nutricional completa
CREATE (a1:Alimento {
    id: 1, 
    nombre: 'Manzana', 
    categoria: 'FRUTAS',
    descripcion: 'Fruta fresca rica en fibra',
    unidadMedida: 'unidad',
    porcion: 1,
    calorias: 95,
    proteinas: 0.5,
    carbohidratos: 25,
    grasas: 0.3,
    fibra: 4.4,
    azucares: 19,
    sodio: 2,
    colesterol: 0,
    esVegetariano: true,
    esVegano: true,
    libre_gluten: true,
    temporada: 'TODO_EL_AÑO',
    alergenos: ''
})

CREATE (a2:Alimento {
    id: 2,
    nombre: 'Pechuga de Pollo',
    categoria: 'PROTEINAS',
    descripcion: 'Pechuga de pollo sin piel',
    unidadMedida: 'gramos',
    porcion: 100,
    calorias: 165,
    proteinas: 31,
    carbohidratos: 0,
    grasas: 3.6,
    fibra: 0,
    azucares: 0,
    sodio: 74,
    colesterol: 85,
    esVegetariano: false,
    esVegano: false,
    libre_gluten: true,
    temporada: 'TODO_EL_AÑO',
    alergenos: ''
})

-- Agregar más alimentos con información nutricional detallada
CREATE (a3:Alimento {
    id: 3,
    nombre: 'Arroz Integral',
    categoria: 'CEREALES',
    descripcion: 'Arroz integral cocido',
    unidadMedida: 'gramos',
    porcion: 100,
    calorias: 130,
    proteinas: 2.7,
    carbohidratos: 28,
    grasas: 0.3,
    fibra: 1.8,
    azucares: 0.3,
    sodio: 5,
    colesterol: 0,
    esVegetariano: true,
    esVegano: true,
    libre_gluten: true,
    temporada: 'TODO_EL_AÑO',
    alergenos: ''
})

CREATE (a4:Alimento {
    id: 4,
    nombre: 'Huevo Entero',
    categoria: 'PROTEINAS',
    descripcion: 'Huevo entero crudo',
    unidadMedida: 'unidad',
    porcion: 1,
    calorias: 72,
    proteinas: 6.3,
    carbohidratos: 0.4,
    grasas: 4.8,
    fibra: 0,
    azucares: 0.2,
    sodio: 71,
    colesterol: 186,
    esVegetariano: true,
    esVegano: false,
    libre_gluten: true,
    temporada: 'TODO_EL_AÑO',
    alergenos: 'huevo'
})

CREATE (a5:Alimento {
    id: 5,
    nombre: 'Espinacas',
    categoria: 'VERDURAS',
    descripcion: 'Espinacas frescas crudas',
    unidadMedida: 'gramos',
    porcion: 100,
    calorias: 23,
    proteinas: 2.9,
    carbohidratos: 3.6,
    grasas: 0.4,
    fibra: 2.2,
    azucares: 0.4,
    sodio: 79,
    colesterol: 0,
    esVegetariano: true,
    esVegano: true,
    libre_gluten: true,
    temporada: 'TODO_EL_AÑO',
    alergenos: ''
})

CREATE (a6:Alimento {
    id: 6,
    nombre: 'Yogur Natural',
    categoria: 'LACTEOS',
    descripcion: 'Yogur natural sin azúcar',
    unidadMedida: 'gramos',
    porcion: 100,
    calorias: 59,
    proteinas: 3.5,
    carbohidratos: 4.7,
    grasas: 3.3,
    fibra: 0,
    azucares: 4.7,
    sodio: 36,
    colesterol: 13,
    esVegetariano: true,
    esVegano: false,
    libre_gluten: true,
    temporada: 'TODO_EL_AÑO',
    alergenos: 'lactosa'
})

-- Crear nodos de categorización
CREATE (catFrutas:CategoriaAlimento {nombre: 'FRUTAS'})
CREATE (catProteinas:CategoriaAlimento {nombre: 'PROTEINAS'})
CREATE (catLacteos:CategoriaAlimento {nombre: 'LACTEOS'})
CREATE (catCereales:CategoriaAlimento {nombre: 'CEREALES'})
CREATE (catVerduras:CategoriaAlimento {nombre: 'VERDURAS'})

-- Crear nodos de nutrientes
CREATE (carbohidratos:Nutriente {nombre: 'Carbohidratos', unidad: 'g'})
CREATE (proteinas:Nutriente {nombre: 'Proteinas', unidad: 'g'})
CREATE (grasas:Nutriente {nombre: 'Grasas', unidad: 'g'})

-- Relacionar alimentos con categorías y nutrientes
MATCH (a:Alimento), (cat:CategoriaAlimento)
WHERE a.categoria = cat.nombre
CREATE (a)-[:PERTENECE_A]->(cat)

MATCH (a:Alimento), (n:Nutriente)
WHERE n.nombre IN ['Carbohidratos', 'Proteinas', 'Grasas']
CREATE (a)-[:CONTIENE {
    cantidad: CASE n.nombre 
        WHEN 'Carbohidratos' THEN a.carbohidratos
        WHEN 'Proteinas' THEN a.proteinas
        WHEN 'Grasas' THEN a.grasas
    END
}]->(n)

-- Agregar relaciones de compatibilidad
CREATE (a3)-[:COMPATIBLE_CON]->(a5)  -- Arroz integral compatible con espinacas
CREATE (a4)-[:COMPATIBLE_CON]->(a5)  -- Huevo compatible con espinacas
CREATE (a5)-[:COMPATIBLE_CON]->(a2)  -- Espinacas compatible con pollo
CREATE (a6)-[:COMPATIBLE_CON]->(a1)  -- Yogur compatible con manzana


-- Crear Nutricionistas
CREATE (n1:Nutricionista {
    id: 1,
    nombre: 'admin',
    apellido: 'admin',
    fotoPerfil: 'nutricionista1.jpg', -- Nuevo atributo
    email: 'ana.garcia@nutricion.com',
    password: '$2a$10$encrypted',
    especialidad: 'Nutrición Deportiva',
    numeroLicencia: 'NUT-2024-001',
    universidad: 'Universidad Nacional',
    añosExperiencia: 5,
    especialidades: 'Deportiva,Clínica',
    descripcionProfesional: 'Especialista en nutrición deportiva y pérdida de peso',
    telefono: '555-0101',
    direccionConsultorio: 'Av. Principal 123',
    horarioAtencion: 'Lunes a Viernes 9:00-17:00',
    tarifaConsulta: 80.00,
    metodoPago: 'EFECTIVO,TARJETA',
    disponibleConsultaOnline: true,
    duracionConsultaMinutos: 60,
    activo: true,
    rol: 'ROLE_NUTRICIONISTA'
})

-- Crear Nutricionistas
CREATE (n1:Nutricionista {
    id: 1,
    nombre: 'Ana',
    apellido: 'García',
    fotoPerfil: 'nutricionista1.jpg', -- Nuevo atributo
    email: 'ana.garcia@nutricion.com',
    password: '$2a$10$encrypted',
    especialidad: 'Nutrición Deportiva',
    numeroLicencia: 'NUT-2024-001',
    universidad: 'Universidad Nacional',
    añosExperiencia: 5,
    especialidades: 'Deportiva,Clínica',
    descripcionProfesional: 'Especialista en nutrición deportiva y pérdida de peso',
    telefono: '555-0101',
    direccionConsultorio: 'Av. Principal 123',
    horarioAtencion: 'Lunes a Viernes 9:00-17:00',
    tarifaConsulta: 80.00,
    metodoPago: 'EFECTIVO,TARJETA',
    disponibleConsultaOnline: true,
    duracionConsultaMinutos: 60,
    activo: true,
    rol: 'ROLE_NUTRICIONISTA'
})

-- Crear Pacientes
CREATE (p1:Paciente {
    id: 1,
    nombre: 'Carlos',
    apellido: 'Martínez',
    edad: 30,
    genero: 'MASCULINO',
    fotoPerfil: 'paciente1.jpg', -- Nuevo atributo
    email: 'carlos.martinez@email.com',
    password: '$2a$10$encrypted',
    peso: 85.5,
    estatura: 1.75,
    telefono: '555-0202',
    direccion: 'Calle 45 #789',
    enfermedadesCronicas: 'Ninguna',
    alergias: 'Nueces',
    grupoSanguineo: 'O+',
    actividadFisica: 'MODERADO',
    fechaRegistro: date(),
    pesoObjetivo: 80.0,
    activo: true,
    rol: 'ROLE_PACIENTE'
})

-- Crear Planes de Dieta
CREATE (pd1:PlanDieta {
    id: 1,
    nombre: 'Plan Pérdida de Peso Saludable',
    descripcion: 'Plan equilibrado para pérdida de peso gradual',
    fechaInicio: date(),
    fechaFin: date() + duration('P3M'),
    objetivo: 'PERDIDA_PESO',
    caloriasObjetivo: 2000,
    proteinasObjetivo: 100,
    carbohidratosObjetivo: 250,
    grasasObjetivo: 66,
    esVegetariano: false,
    esVegano: false,
    libreGluten: false,
    activo: true,
    estado: 'EN_CURSO'
})

-- Crear Citas
CREATE (c1:Cita {
    id: 1,
    fecha: datetime(),
    estado: 'PROGRAMADA',
    notas: 'Primera consulta - Evaluación inicial'
})

-- Crear Seguimiento de Peso
CREATE (sp1:SeguimientoPeso {
    id: 1,
    fechaRegistro: datetime(),
    peso: 85.5,
    imc: 27.9,
    porcentajeGrasaCorporal: 25.0,
    circunferenciaCintura: 95,
    circunferenciaCadera: 105,
    presionArterialSistolica: 120,
    presionArterialDiastolica: 80
})

-- Crear Relaciones
CREATE (n1)-[:ATIENDE]->(p1)
CREATE (n1)-[:CREATES_PLAN]->(pd1)
CREATE (p1)-[:HAS_PLAN]->(pd1)
CREATE (pd1)-[:INCLUDES]->(a1)
CREATE (pd1)-[:INCLUDES]->(a2)
CREATE (p1)-[:TIENE_CITA]->(c1)
CREATE (n1)-[:AGENDA_CON]->(c1)
CREATE (p1)-[:TIENE_SEGUIMIENTO]->(sp1)
CREATE (a1)-[:COMPATIBLE_CON]->(a2)

-- Relaciones Alimentarias Detalladas
CREATE (a1)-[:CONTIENE_NUTRIENTE {cantidad: 25}]->(carbohidratos)
CREATE (a1)-[:CONTIENE_NUTRIENTE {cantidad: 0.5}]->(proteinas)
CREATE (a1)-[:CONTIENE_NUTRIENTE {cantidad: 0.3}]->(grasas)
CREATE (a2)-[:CONTIENE_NUTRIENTE {cantidad: 0}]->(carbohidratos)
CREATE (a2)-[:CONTIENE_NUTRIENTE {cantidad: 31}]->(proteinas)
CREATE (a2)-[:CONTIENE_NUTRIENTE {cantidad: 3.6}]->(grasas)

-- Relaciones de Categorización
CREATE (a1)-[:ES_TIPO]->(catFrutas)
CREATE (a2)-[:ES_TIPO]->(catProteinas)

-- Relaciones de Condiciones de Salud
CREATE (a1)-[:RECOMENDADO_PARA]->(diabetes:CondicionSalud {nombre: 'Diabetes'})
CREATE (a1)-[:RECOMENDADO_PARA]->(hipertension:CondicionSalud {nombre: 'Hipertensión'})
CREATE (a2)-[:CONTRAINDICADO_PARA]->(colesterol:CondicionSalud {nombre: 'Colesterol Alto'})

-- Relaciones de Preferencias y Restricciones de Pacientes
CREATE (p1)-[:RESTRINGE {motivo: 'Alergia'}]->(nueces:Alimento {nombre: 'Nueces'})
CREATE (p1)-[:PREFIERE {nivel: 'ALTO'}]->(a1)
CREATE (p1)-[:EVITA {motivo: 'Intolerancia'}]->(lacteos:Alimento {nombre: 'Lacteos'})

-- Relaciones de Experiencia Profesional
CREATE (n1)-[:ESPECIALIZADO_EN]->(especDeportiva:Especialidad {nombre: 'Nutrición Deportiva'})
CREATE (n1)-[:ESPECIALIZADO_EN]->(especClinica:Especialidad {nombre: 'Nutrición Clínica'})
CREATE (n1)-[:TRABAJA_EN]->(clinica:Clinica {nombre: 'Centro Médico Nutrición'})

-- Relaciones de Seguimiento y Mediciones
CREATE (p1)-[:REGISTRA]->(medicion:Medicion {
    fecha: datetime(),
    peso: 85.5,
    imc: 27.9,
    porcentajeGrasaCorporal: 25.0,
    circunferenciaCintura: 95,
    circunferenciaCadera: 105
})

-- Relaciones de Objetivos
CREATE (p1)-[:TIENE_OBJETIVO {
    fechaInicio: date(),
    fechaFin: date() + duration('P3M'),
    pesoObjetivo: 80.0
}]->(objetivo:ObjetivoNutricional {
    tipo: 'PERDIDA_PESO',
    descripcion: 'Reducción gradual y saludable de peso'
})

-- Relaciones de Planificación de Comidas
CREATE (pd1)-[:TIENE_COMIDA]->(desayuno:Comida {
    tipo: 'DESAYUNO',
    hora: time('08:00'),
    caloriasTotales: 350
})
CREATE (pd1)-[:TIENE_COMIDA]->(almuerzo:Comida {
    tipo: 'ALMUERZO',
    hora: time('13:00'),
    caloriasTotales: 650
})
CREATE (desayuno)-[:INCLUYE {porcion: 1, unidad: 'unidad'}]->(a1)
CREATE (almuerzo)-[:INCLUYE {porcion: 100, unidad: 'gramos'}]->(a2)

-- Crear Relaciones
CREATE (n1)-[:ATIENDE]->(p1)
CREATE (n1)-[:CREATES_PLAN]->(pd1)
CREATE (p1)-[:HAS_PLAN]->(pd1)
CREATE (pd1)-[:INCLUDES]->(a1)
CREATE (pd1)-[:INCLUDES]->(a2)
CREATE (p1)-[:TIENE_CITA]->(c1)
CREATE (n1)-[:AGENDA_CON]->(c1)
CREATE (p1)-[:TIENE_SEGUIMIENTO]->(sp1)
CREATE (a1)-[:COMPATIBLE_CON]->(a2)

-- Relaciones Alimentarias
CREATE (a1)-[:CONTIENE_NUTRIENTE {cantidad: 14}]->(nutriente:Nutriente {nombre: 'Carbohidratos'})
CREATE (a1)-[:ES_TIPO]->(categoria:CategoriaAlimento {nombre: 'FRUTAS'})
CREATE (a1)-[:RECOMENDADO_PARA]->(condicion:CondicionSalud {nombre: 'Diabetes'})

-- Restricciones y Preferencias
CREATE (p1)-[:RESTRINGE {motivo: 'Alergia'}]->(a2)
CREATE (p1)-[:PREFIERE {nivel: 'ALTO'}]->(a1)
CREATE (p1)-[:EVITA {motivo: 'Intolerancia'}]->(alimento:Alimento {nombre: 'Lacteos'})

-- Relaciones Profesionales
CREATE (n1)-[:ESPECIALIZADO_EN]->(esp:Especialidad {nombre: 'Nutrición Deportiva'})
CREATE (n1)-[:TRABAJA_EN]->(clinica:Clinica {nombre: 'Centro Médico'})

-- Relaciones de Seguimiento
CREATE (p1)-[:REGISTRA]->(medicion:Medicion {
    fecha: datetime(),
    peso: 85.5,
    imc: 27.9
})
CREATE (p1)-[:TIENE_OBJETIVO {
    fecha_inicio: date(),
    fecha_fin: date() + duration('P3M'),
    peso_objetivo: 80.0
}]->(objetivo:ObjetivoNutricional {tipo: 'PERDIDA_PESO'})

-- Relaciones de Plan de Dieta
CREATE (pd1)-[:PARA_CONDICION]->(condicion)
CREATE (pd1)-[:TIENE_COMIDA]->(comida:Comida {
    tipo: 'DESAYUNO',
    hora: time('08:00'),
    calorias_totales: 350
})
CREATE (comida)-[:INCLUYE {porcion: 100, unidad: 'g'}]->(a2)

-- Relaciones de Citas y Seguimiento
CREATE (c1)-[:TIENE_PROPOSITO]->(proposito:PropositoCita {tipo: 'EVALUACION_INICIAL'})
CREATE (sp1)-[:REGISTRA_MEDIDAS]->(medidas:MedidasCorporales {
    circunferencia_cintura: 95,
    circunferencia_cadera: 105
})

-- Crear nodos de horarios y relacionarlos con citas
MATCH (c:Cita)
CREATE (h:Horario {
    dia_semana: ['LUNES', 'MARTES', 'MIERCOLES', 'JUEVES', 'VIERNES'][toInteger(rand() * 5)],
    hora_inicio: c.fecha.hour,
    duracion: 60
})
CREATE (c)-[:PROGRAMADO_EN]->(h)

-- Relacionar mediciones con pacientes y planes
MATCH (p:Paciente), (m:Medicion)
WHERE m.fecha >= p.fechaRegistro
CREATE (p)-[:REGISTRA]->(m)

-- Crear relaciones de compatibilidad entre alimentos
MATCH (a1:Alimento), (a2:Alimento)
WHERE a1 <> a2 AND NOT EXISTS((a1)-[:COMPATIBLE_CON]->(a2))
WITH a1, a2
WHERE rand() > 0.7
CREATE (a1)-[:COMPATIBLE_CON]->(a2)

-- Relacionar nutricionistas con especialidades
MATCH (n:Nutricionista)
UNWIND split(n.especialidades, ',') AS esp
MERGE (e:Especialidad {nombre: trim(esp)})
CREATE (n)-[:ESPECIALIZADO_EN]->(e)

-- Crear relaciones de seguimiento completas
MATCH (p:Paciente), (sp:SeguimientoPeso)
WHERE NOT EXISTS((p)-[:TIENE_SEGUIMIENTO]->(sp))
CREATE (p)-[:TIENE_SEGUIMIENTO]->(sp)

-- Crear objetivos nutricionales y relacionarlos
MATCH (p:Paciente)
CREATE (obj:ObjetivoNutricional {
    tipo: p.objetivoNutricional,
    fecha_inicio: p.fechaRegistro,
    fecha_fin: p.fechaRegistro + duration('P3M'),
    descripcion: 'Objetivo personalizado'
})
CREATE (p)-[:TIENE_OBJETIVO]->(obj)

-- Crear relaciones entre alimentos y condiciones de salud
MATCH (a:Alimento)
WHERE a.categoria IN ['FRUTAS', 'VERDURAS']
CREATE (c:CondicionSalud {nombre: 'Diabetes'})
CREATE (a)-[:RECOMENDADO_PARA]->(c)

-- Asegurar que todas las citas tengan propósito
MATCH (c:Cita)
WHERE NOT EXISTS((c)-[:TIENE_PROPOSITO]->())
CREATE (p:PropositoCita {
    tipo: 'SEGUIMIENTO',
    descripcion: 'Consulta de seguimiento'
})
CREATE (c)-[:TIENE_PROPOSITO]->(p)

-- Crear índices para mejor rendimiento
CREATE INDEX alimento_nombre_index IF NOT EXISTS FOR (n:Alimento) ON (n.nombre)
CREATE INDEX nutricionista_email_index IF NOT EXISTS FOR (n:Nutricionista) ON (n.email)
CREATE INDEX paciente_email_index IF NOT EXISTS FOR (n:Paciente) ON (n.email)
CREATE INDEX plandieta_nombre_index IF NOT EXISTS FOR (n:PlanDieta) ON (n.nombre)

-- Índices adicionales para optimización
CREATE INDEX IF NOT EXISTS FOR (n:Comida) ON (n.tipo)
CREATE INDEX IF NOT EXISTS FOR (n:CondicionSalud) ON (n.nombre)
CREATE INDEX IF NOT EXISTS FOR (n:Especialidad) ON (n.nombre)

-- Crear índices compuestos para mejor rendimiento
CREATE INDEX alimento_categoria_index IF NOT EXISTS FOR (n:Alimento) ON (n.categoria, n.nombre)
CREATE INDEX medicion_fecha_index IF NOT EXISTS FOR (n:Medicion) ON (n.fecha)
CREATE INDEX cita_estado_index IF NOT EXISTS FOR (n:Cita) ON (n.estado)

-- Constraints para integridad de datos
CREATE CONSTRAINT IF NOT EXISTS FOR (n:Alimento) REQUIRE n.nombre IS UNIQUE
CREATE CONSTRAINT IF NOT EXISTS FOR (n:Nutricionista) REQUIRE n.numeroLicencia IS UNIQUE
CREATE CONSTRAINT IF NOT EXISTS FOR (n:Paciente) REQUIRE n.email IS UNIQUE

-- Crear índice para búsqueda por nombre de nutricionista
CREATE INDEX nutricionista_nombre_index IF NOT EXISTS FOR (n:Nutricionista) ON (n.nombre);

-- Vincular pacientes existentes con admin
MATCH (n:Nutricionista), (p:Paciente)
WHERE n.nombre = 'admin'
CREATE (n)-[:ATIENDE]->(p);

// Queries útiles como comentarios
/*
// Encontrar todos los alimentos recomendados para una condición
MATCH (a:Alimento)-[:RECOMENDADO_PARA]->(c:CondicionSalud {nombre: 'Diabetes'})
RETURN a

// Encontrar planes de dieta compatibles con las restricciones del paciente
MATCH (p:Paciente)-[:RESTRINGE]->(a:Alimento),
      (pd:PlanDieta)-[:INCLUDES]->(a2:Alimento)
WHERE NOT a = a2
RETURN pd

// Calcular progreso del paciente
MATCH (p:Paciente)-[:REGISTRA]->(m:Medicion)
RETURN p.nombre, 
       m.peso, 
       m.fecha
ORDER BY m.fecha DESC
*/

// Para nutricionistas
MATCH (n:Nutricionista)
WHERE n.fotoPerfil IS NULL
SET n.fotoPerfil = CASE n.genero 
    WHEN 'MASCULINO' THEN 'nutricionista-male-default.png'
    WHEN 'FEMENINO' THEN 'nutricionista-female-default.png'
    ELSE 'nutricionista-default.png'
END;

// Para pacientes
MATCH (p:Paciente)
WHERE p.fotoPerfil IS NULL
SET p.fotoPerfil = CASE p.genero 
    WHEN 'MASCULINO' THEN 'paciente-male-default.png'
    WHEN 'FEMENINO' THEN 'paciente-female-default.png'
    ELSE 'paciente-default.png'
END;
