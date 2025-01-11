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

CREATE (catFrutas:CategoriaAlimento {nombre: 'FRUTAS'})
CREATE (catProteinas:CategoriaAlimento {nombre: 'PROTEINAS'})
CREATE (catLacteos:CategoriaAlimento {nombre: 'LACTEOS'})
CREATE (catCereales:CategoriaAlimento {nombre: 'CEREALES'})
CREATE (catVerduras:CategoriaAlimento {nombre: 'VERDURAS'})

CREATE (carbohidratos:Nutriente {nombre: 'Carbohidratos', unidad: 'g'})
CREATE (proteinas:Nutriente {nombre: 'Proteinas', unidad: 'g'})
CREATE (grasas:Nutriente {nombre: 'Grasas', unidad: 'g'})

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

CREATE (a3)-[:COMPATIBLE_CON]->(a5)  
CREATE (a4)-[:COMPATIBLE_CON]->(a5)  
CREATE (a5)-[:COMPATIBLE_CON]->(a2)  
CREATE (a6)-[:COMPATIBLE_CON]->(a1)  

 