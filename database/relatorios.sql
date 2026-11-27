-- Relatorio 1: Media, menor e maior nota por codigo de avaliacao filtrada por periodo e turma, comparada com a media geral
SELECT
    av.codigo,
    av.titulo,
    COUNT(notas_aluno.id_aluno) AS qtd_respondentes,
    ROUND(AVG(notas_aluno.nota_total), 2) AS media_avaliacao,
    ROUND(MIN(notas_aluno.nota_total), 2) AS menor_nota,
    ROUND(MAX(notas_aluno.nota_total), 2) AS maior_nota,
    ROUND((
        SELECT AVG(sub.total)
        FROM (
            SELECT SUM(r2.nota) AS total
            FROM Resposta r2
            GROUP BY r2.ID_aluno, r2.codigo_avaliacao
        ) sub
    ), 2) AS media_geral_sistema
FROM Avaliacao av
INNER JOIN (
    SELECT
        s.ID_aluno AS id_aluno,
        s.codigo_avaliacao,
        s.data,
        mt.codigo_turma,
        SUM(r.nota) AS nota_total
    FROM Soluciona_avaliacao s
    INNER JOIN Aluno al ON al.ID=s.ID_aluno
    INNER JOIN Matricula_turma mt ON mt.matricula=al.matricula
    INNER JOIN Resposta r
        ON r.ID_aluno=s.ID_aluno
       AND r.codigo_avaliacao=s.codigo_avaliacao
    WHERE s.data BETWEEN '2026-01-01' AND '2026-12-31'
    GROUP BY s.ID_aluno, s.codigo_avaliacao, s.data, mt.codigo_turma
) notas_aluno ON notas_aluno.codigo_avaliacao=av.codigo
GROUP BY av.codigo, av.titulo
HAVING COUNT(notas_aluno.id_aluno)>=1
ORDER BY media_avaliacao DESC;

-- Relatorio 2: Ranking das 10 questoes com maior percentual de erro filtradas por termo no enunciado
SELECT
    q.codigo,
    q.enunciado,
    COUNT(r.ID_aluno) AS total_respostas,
    ROUND(AVG(COALESCE(r.nota/NULLIF(ia.valor, 0), 0)*100.0), 1) AS percentual_acerto,
    ROUND(100.0-AVG(COALESCE(r.nota/NULLIF(ia.valor, 0), 0)*100.0), 1) AS percentual_erro,
    RANK() OVER (ORDER BY AVG(COALESCE(r.nota/NULLIF(ia.valor, 0), 0)) ASC) AS ranking_dificuldade
FROM Questao q
INNER JOIN Item_avaliacao ia ON ia.codigo_questao=q.codigo
INNER JOIN Resposta r
    ON r.codigo_avaliacao=ia.codigo_avaliacao
   AND r.codigo_questao=ia.codigo_questao
WHERE LOWER(q.enunciado) LIKE LOWER('%%')
GROUP BY q.codigo, q.enunciado
ORDER BY ranking_dificuldade ASC, total_respostas DESC
FETCH FIRST 10 ROWS ONLY;

-- Relatorio 3: Evolucao de desempenho por aluno, turma ou geral ao longo do tempo comparada com a media de referencia
SELECT
    s.data,
    av.codigo,
    av.titulo,
    ROUND(SUM(r.nota), 2) AS nota_selecionada,
    ROUND((
        SELECT AVG(outros.total_colega)
        FROM (
            SELECT SUM(r2.nota) AS total_colega
            FROM Resposta r2
            WHERE r2.codigo_avaliacao=av.codigo
            GROUP BY r2.ID_aluno
        ) outros
    ), 2) AS media_referencia
FROM Soluciona_avaliacao s
INNER JOIN Avaliacao av ON av.codigo=s.codigo_avaliacao
INNER JOIN Resposta r
    ON r.ID_aluno=s.ID_aluno
   AND r.codigo_avaliacao=s.codigo_avaliacao
WHERE s.ID_aluno=2
GROUP BY s.data, av.codigo, av.titulo
ORDER BY s.data ASC;

-- Relatorio 4A: Resumo de alunos que responderam vs pendentes por codigo de avaliacao e turma usando LEFT JOIN
SELECT
    av.codigo,
    av.titulo,
    t.codigo AS codigo_turma,
    t.nome AS turma,
    COUNT(al.ID) AS total_destinados,
    COUNT(s.ID_aluno) AS responderam,
    COUNT(al.ID)-COUNT(s.ID_aluno) AS pendentes
FROM Avaliacao av
INNER JOIN Destinada d ON d.codigo_avaliacao=av.codigo
INNER JOIN Turma t ON t.codigo=d.codigo_turma
INNER JOIN Matricula_turma mt ON mt.codigo_turma=t.codigo
INNER JOIN Aluno al ON al.matricula=mt.matricula
LEFT JOIN Soluciona_avaliacao s
    ON s.ID_aluno=al.ID
   AND s.codigo_avaliacao=av.codigo
WHERE t.codigo IN (101, 102, 103)
GROUP BY av.codigo, av.titulo, t.codigo, t.nome
ORDER BY av.codigo, t.nome;

-- Relatorio 4B: Lista nominal dos alunos que ainda nao responderam cada avaliacao por turma
SELECT
    av.codigo AS codigo_avaliacao,
    av.titulo AS avaliacao,
    t.codigo AS codigo_turma,
    t.nome AS turma,
    al.ID AS id_aluno,
    al.nome AS aluno,
    al.email
FROM Avaliacao av
INNER JOIN Destinada d ON d.codigo_avaliacao=av.codigo
INNER JOIN Turma t ON t.codigo=d.codigo_turma
INNER JOIN Matricula_turma mt ON mt.codigo_turma=t.codigo
INNER JOIN Aluno al ON al.matricula=mt.matricula
LEFT JOIN Soluciona_avaliacao s
    ON s.ID_aluno=al.ID
   AND s.codigo_avaliacao=av.codigo
WHERE t.codigo IN (101, 102, 103)
  AND s.ID_aluno IS NULL
ORDER BY av.codigo, t.nome, al.nome;

-- Relatorio 4C: Questoes cadastradas que nunca foram utilizadas em nenhuma avaliacao
SELECT
    q.codigo,
    CASE WHEN qo.codigo IS NOT NULL THEN 'OBJETIVA' ELSE 'DISSERTATIVA' END AS tipo,
    q.enunciado
FROM Questao q
LEFT JOIN Questao_objetiva qo ON qo.codigo=q.codigo
LEFT JOIN Item_avaliacao ia ON ia.codigo_questao=q.codigo
WHERE ia.codigo_avaliacao IS NULL
ORDER BY q.codigo;
