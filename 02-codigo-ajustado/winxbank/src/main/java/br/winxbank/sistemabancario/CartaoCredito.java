package br.winxbank.sistemabancario;

import br.winxbank.tempo.Ano;

import java.util.Scanner;
import java.util.logging.Logger;

public class CartaoCredito extends Cartao implements OperacoesAutomaticas {

    private static final Logger LOGGER = Logger.getLogger(CartaoCredito.class.getName());

    private double fatura;
    private String mesDaFatura;
    private int indexMesDaFatura;
    private boolean faturaPaga;
    private double limite;

    public CartaoCredito(int numero, int csv) {
        super(numero, csv);
        this.limite = 1000;
    }

    public CartaoCredito(double fatura, int indexMesDaFatura, boolean faturaPaga,
                         double limite, int numero, int csv) {
        super(numero, csv);
        this.fatura = fatura;
        this.indexMesDaFatura = indexMesDaFatura;
        this.faturaPaga = faturaPaga;
        this.limite = limite;
    }

    public void creditar(double valor) {
        creditar(valor, Ano.getInstancia().getIndexMesAtual(), Ano.getInstancia().getMesAtual());
    }

    public void creditar(double valor, int indexMesAtual, String mesAtual) {
        setFatura(valor);
        this.indexMesDaFatura = indexMesAtual;
        this.mesDaFatura = mesAtual;
    }

    public void ajustarLimite() {
        ajustarLimite(new Scanner(System.in));
    }

    public void ajustarLimite(Scanner sc) {
        LOGGER.info("Digite o valor do limite do seu cartão que deseja ajustar: ");
        double novoLimite = sc.nextDouble();
        ajustarLimite(novoLimite);
    }

    public void ajustarLimite(double novoLimite) {
        if (novoLimite <= 0) {
            throw new IllegalArgumentException("O limite deve ser maior que zero.");
        }

        this.limite = novoLimite;
    }

    public void setFatura(double valor) {
        if (valor + fatura <= this.limite) {
            this.fatura += valor;
        }

        this.faturaPaga = this.fatura <= 0;
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

    public void cobrarJurus() {
        cobrarJurus(Ano.getInstancia().getIndexMesAtual());
    }

    public void cobrarJurus(int indexMesAtual) {
        if (!this.faturaPaga && indexMesAtual > this.indexMesDaFatura) {
            double faturaAnterior = this.fatura;
            this.fatura *= taxaJurus;
            movimentacaoBancaria(this.fatura - faturaAnterior);
        }
    }

    public void movimentacaoBancaria(double valor) {
        Banco.getInstancia().setReceitas(valor);
    }
}