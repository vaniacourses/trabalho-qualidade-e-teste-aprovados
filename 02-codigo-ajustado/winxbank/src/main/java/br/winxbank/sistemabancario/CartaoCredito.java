package br.winxbank.sistemabancario;

import br.winxbank.tempo.Ano;

import java.util.Scanner;
import java.util.logging.Logger;

/**
 * @author Carol
 * Esta classe é responsável por representar uma entidade CartaoCredito.
 */
public class CartaoCredito extends Cartao implements OperacoesAutomaticas {

    private static final Logger LOGGER = Logger.getLogger(CartaoCredito.class.getName());
	
    private double fatura;
    private String mesDaFatura;
    private int indexMesDaFatura;
    private boolean faturaPaga;
    private double limite;

    /**
     * Construtor padrão do cartão de crédito.
     * 
     * @param numero número do cartão
     * @param csv código de segurança
     */
    public CartaoCredito(int numero, int csv) {
        super(numero, csv);
        this.limite = 1000;
    }

    /**
     * Construtor alternativo para leitura de json e testes.
     * 
     * @param fatura valor atual da fatura
     * @param indexMesDaFatura índice do mês da fatura
     * @param faturaPaga status da fatura
     * @param limite limite do cartão
     * @param numero número do cartão
     * @param csv código de segurança
     */
    public CartaoCredito(double fatura, int indexMesDaFatura, boolean faturaPaga, double limite, int numero, int csv) {
        super(numero, csv);
        this.fatura = fatura;
        this.indexMesDaFatura = indexMesDaFatura;
        this.faturaPaga = faturaPaga;
        this.limite = limite;
    }

    /**
     * Método responsável por creditar um valor na fatura do cartão de crédito.
     * Mantém o comportamento original, usando o mês atual do Singleton Ano.
     * 
     * @param valor valor a ser creditado
     */
    public void creditar(double valor) {
        creditar(valor, Ano.getInstancia().getIndexMesAtual(), Ano.getInstancia().getMesAtual());
    }

    /**
     * Versão testável do método creditar.
     * Permite informar diretamente o mês e o índice do mês, evitando dependência direta
     * do Singleton Ano durante o teste unitário.
     * 
     * @param valor valor a ser creditado
     * @param indexMesAtual índice do mês atual
     * @param mesAtual nome do mês atual
     */
    public void creditar(double valor, int indexMesAtual, String mesAtual) {
        setFatura(valor);
        this.indexMesDaFatura = indexMesAtual;
        this.mesDaFatura = mesAtual;
    }

    /**
     * Método responsável por ajustar o limite do cartão usando entrada do console.
     * Mantém o comportamento esperado pela aplicação principal.
     */
    public void ajustarLimite() {
        Scanner sc = new Scanner(System.in);

        LOGGER.info("Digite o valor do limite do seu cartão que deseja ajustar: ");
        double novoLimite = sc.nextDouble();

        ajustarLimite(novoLimite);
    }

    /**
     * Versão testável do ajuste de limite.
     * Centraliza a regra de negócio e evita dependência de Scanner/System.in nos testes.
     * 
     * @param novoLimite novo limite informado
     */
    public void ajustarLimite(double novoLimite) {
        if (novoLimite <= 0) {
            throw new IllegalArgumentException("O limite deve ser maior que zero.");
        }

        this.limite = novoLimite;
    }

    /**
     * Setter com regra de negócio: se o valor somado à fatura atual for menor ou igual
     * ao limite, permite a alteração da fatura. Também atualiza o status da fatura paga.
     * 
     * @param valor valor a ser aplicado na fatura
     */
    public void setFatura(double valor) {
        if (valor + fatura <= this.limite) {
            this.fatura += valor;
        }

        if (this.fatura <= 0) {
            this.faturaPaga = true;
        } else {
            this.faturaPaga = false;
        }
    }

    public double getFatura() {
        return fatura;
    }

    public boolean isFaturaPaga() {
        return faturaPaga;
    }

    public String getMesDaFatura() {
        return mesDaFatura;
    }

    public int getIndexMesDaFatura() {
        return indexMesDaFatura;
    }

    public double getLimite() {
        return limite;
    }

    /**
     * Método responsável por cobrar juros de uma fatura conforme meses passados.
     * Mantém o comportamento original, usando o mês atual do Singleton Ano.
     */
    public void cobrarJuros() {
        cobrarJuros(Ano.getInstancia().getIndexMesAtual());
    }

    /**
     * Versão testável da cobrança de juros.
     * Permite informar diretamente o índice do mês atual, facilitando o teste unitário
     * sem depender do Singleton Ano.
     * 
     * @param indexMesAtual índice do mês atual
     */
    public void cobrarJuros(int indexMesAtual) {
        if (!this.faturaPaga && indexMesAtual > this.indexMesDaFatura) {
            double faturaAnterior = this.fatura;
            this.fatura *= taxaJuros;
            movimentacaoBancaria(this.fatura - faturaAnterior);
        }
    }

    /**
     * Método responsável por gerar receita ao banco do valor pago a mais da cobrança
     * de juros em cima de uma fatura.
     * 
     * @param valor valor movimentado
     */
    public void movimentacaoBancaria(double valor) {
        Banco.getInstancia().setReceitas(valor);
    }
}