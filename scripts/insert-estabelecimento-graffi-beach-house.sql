BEGIN;

DO $$
DECLARE
    v_usuario_id INTEGER;
    v_papel_empresario_id INTEGER;
    v_estabelecimento_id INTEGER;
BEGIN
    SELECT u.id
      INTO v_usuario_id
      FROM website.usuario u
     WHERE u.email = 'hostelgraffi@gmail.com';

    IF v_usuario_id IS NULL THEN
        RAISE EXCEPTION 'Usuário com e-mail % não foi encontrado.', 'hostelgraffi@gmail.com';
    END IF;

    INSERT INTO website.papeis (id, nome, codigo)
    SELECT nextval('website.seq_papeis'), 'Empresário', 'empresario'
    WHERE NOT EXISTS (
        SELECT 1
          FROM website.papeis
         WHERE codigo = 'empresario'
    );

    SELECT p.id
      INTO v_papel_empresario_id
      FROM website.papeis p
     WHERE p.codigo = 'empresario';

    -- Alinha a sequência ao maior id da tabela (evita erro de PK quando a sequência ficou defasada)
    PERFORM setval(
        'website.seq_usuario_papel',
        COALESCE((SELECT MAX(id) FROM website.usuario_papel), 0)
    );

    INSERT INTO website.usuario_papel (usuario_id, papel_id)
    VALUES (v_usuario_id, v_papel_empresario_id)
    ON CONFLICT (usuario_id, papel_id) DO NOTHING;

    SELECT e.id
      INTO v_estabelecimento_id
      FROM website.estabelecimento e
     WHERE e.nome = 'Graffi Beach House - Hospedagem, Restaurante e Festas'
       AND e.endereco = 'R. Aroeira do Campo, 25B - Campeche, Florianópolis - SC, 88066-280';

    IF v_estabelecimento_id IS NULL THEN
        INSERT INTO website.estabelecimento (
            nome,
            latitude,
            longitude,
            endereco,
            ativo,
            usuario_proprietario_id
        )
        VALUES (
            'Graffi Beach House - Hospedagem, Restaurante e Festas',
            -27.706228,
            -48.497777,
            'R. Aroeira do Campo, 25B - Campeche, Florianópolis - SC, 88066-280',
            TRUE,
            v_usuario_id
        )
        RETURNING id INTO v_estabelecimento_id;
    ELSE
        UPDATE website.estabelecimento
           SET latitude = -27.706228,
               longitude = -48.497777,
               ativo = TRUE,
               usuario_proprietario_id = v_usuario_id
         WHERE id = v_estabelecimento_id;
    END IF;

    RAISE NOTICE 'Estabelecimento processado com sucesso. ID: %', v_estabelecimento_id;
END $$;

COMMIT;
