package br.winxbank.sistemaclientes;

import br.winxbank.sistemabancario.*;

import java.util.ArrayList;
import java.util.Scanner;

import java.text.DecimalFormat;

/**
 * @author Natália
 * Esta classe é responsável por administrar um registro de clientes.
 */
public class RegistroDeClientes {

    private static RegistroDeClientes instancia;
    private ArrayList<Cliente> clientes = new ArrayList<Cliente>();

    /**
     * Este método é responsável por cadastrar um cliente no registro de clientes.
     * Se o cliente criar uma conta com mais de 100 mil, ele se torna ClienteWix.
     */
    public void cadastrarCliente(){
        Scanner sc = new Scanner(System.in);
        System.out.println("Você está cadastrando um cliente\nDigite o nome:");
        String nome = sc.nextLine();
        System.out.println("Digite o cpf:");
        String cpf = sc.nextLine();
        boolean cpfDisponivel = checarCpf(cpf);
        if(cpfDisponivel){
            Cliente cliente = new Cliente(nome, cpf);
            Conta conta = Banco.getInstancia().abrirNovaConta();
            cliente.setContas(conta);
            Movimentacao movimentacao = new Movimentacao(conta.getSaldo(), Movimentacao.TipoDaMovimentacao.ENTRADA);
            conta.setExtrato(movimentacao);
            if(conta instanceof ContaPoupanca){
                ((ContaPoupanca) conta).setInformeRendimento(movimentacao);
            }
            if(conta.getSaldo() >= 100000){
                System.out.println("Parabéns, você tem direito a ser ClienteWinx!");
                ClienteWinx clienteWinx = new ClienteWinx(nome, cpf, 0);
                clienteWinx.setContas(conta);
                clientes.add(clienteWinx);
            }
            else{
                clientes.add(cliente);
            }
        }else{
            System.out.println("Usuario nao pode ser criado. CPF ja existente no registro.");
        }
    }

    /**
     * Este método é responsável por atualizar dados de um cliente do registro de clientes
     * @param cliente
     */
    public void atualizarCliente(Cliente cliente) throws InterruptedException {
        System.out.println("Seu usuario está sendo atualizado...");
        for(int i = 0; i < clientes.size(); i++){
            if(clientes.get(i).getCpf().equals(cliente.getCpf())){
                clientes.remove(i);
                clientes.add(cliente);
                break;
            }
        }
    }

    /**
     * Este método é responsável por remover um cliente do registro de clientes.
     * @param cliente
     */
    public void removerCliente(Cliente cliente){
        System.out.println("Seu usuario está sendo apagado...");
        for(int i = 0; i < this.clientes.size(); i++){
            if(this.clientes.get(i).getCpf().equals(cliente.getCpf())){
                this.clientes.remove(i);
                break;
            }
        }
    }

    /**
     * Método responsável por checar se o cpf já existe no registro.
     * @param cpf
     * @return
     */
    public boolean checarCpf(String cpf){
        for (Cliente cliente : clientes){
            if(cliente.getCpf().equals(cpf)){
                return false;
            }
        }
        return true;
    }

    /**
     * Este método é responsável por visualizar contas de um determinado cliente.
     * @param cliente
     */
    public void visualizarContas(Cliente cliente){
        for(Conta conta : cliente.getContas()){
            if(conta instanceof ContaPoupanca contaPoupanca){
                System.out.println("[ Conta" + contaPoupanca.getTipoDaConta() + " no: " + conta.getNumeroConta() + " | Saldo: " + new DecimalFormat("0.00").format( conta.getSaldo()) + " | DividaEmprestimo: " + new DecimalFormat("0.00").format(conta.getDividaDeEmprestimo()) + " | Cartao Debito no: " + conta.getCartao().getNumero() +"| csv: "+ conta.getCartao().getCsv() + " ]");
            }
            else if(conta instanceof ContaCorrente contaCorrente){
                System.out.println("[ Conta" + contaCorrente.getTipoDaConta() + "no: " + conta.getNumeroConta() + " | Saldo: " + new DecimalFormat("0.00").format(conta.getSaldo()) + " | DividaEmprestimo: " + new DecimalFormat("0.00").format(conta.getDividaDeEmprestimo()) + " | Cartao Debito no: " + conta.getCartao().getNumero() +"| csv: "+ conta.getCartao().getCsv() + " | Cartao Credito no: " + contaCorrente.getCartaoCredito().getNumero() + "| csv: "+ contaCorrente.getCartaoCredito().getCsv() + "| fatura: "+  new DecimalFormat("0.00").format(contaCorrente.getCartaoCredito().getFatura()) +" ]");
            }
        }
    }

    /**
     * Este método é responsável por visualizar detalhes de um cliente do registro a partir do seu CPF.
     * @param cpf
     */
    public void visualizarDetalhesDoCliente(String cpf){
        for(Cliente cliente : clientes){
            if(cliente instanceof ClienteWinx clienteWinx && cliente.getCpf().equals(cpf)){
                System.out.println("Nome: " + cliente.getNome() + " | CPF: " + cliente.getCpf() + " | Pontos por compra: " + clienteWinx.getPontosDeCompra() + "\nContas:");
                visualizarContas(cliente);
            }
            else if(!(cliente instanceof ClienteWinx) && cliente.getCpf().equals(cpf)){
                System.out.println("Nome: " + cliente.getNome() + "| CPF: " + cliente.getCpf() + "\nContas:");
                visualizarContas(cliente);
            }
        }
    }

    /**
     * Este método é responsável por retornar um cliente do registro a partir do CPF.
     * @param cpf
     * @return Cliente
     */
    public Cliente retornarCliente(String cpf){
        for (Cliente cliente : clientes) {
            if (cliente.getCpf().equals(cpf)) {
                return cliente;
            }
        }
        return null;
    }

    /**
     * Este método é responsável por exibir a lista de clientes registrados.
     */
    public void printarListaDeClientes(){
        System.out.println("------------------ Clientes --------------------");
        for(Cliente cliente : clientes){
            if(cliente instanceof ClienteWinx clienteWinx){
                System.out.println("Nome: " + cliente.getNome() + " | CPF: " + cliente.getCpf() + " | Pontos por compra: " + clienteWinx.getPontosDeCompra() + "\nContas:");
            }
            else{
                System.out.println("Nome: " + cliente.getNome() + "| CPF: " + cliente.getCpf() + "\nContas:");
            }
            visualizarContas(cliente);
            System.out.println("------------------------------------------------");
        }
    }

    /**
     * Método responsável por resetar o registro de clientes.
     */
    public void limparListaDeClientes(){
        clientes.clear();
    }

    /**
     * Método responsável por adicionar uma coleção inteira ao atributo do tipo ArrayList de clientes da classe para carregar dados registrados em um arquivo de outras vezes que o programa foi executado.
     * @param clientes
     */
    public void setClientes(ArrayList<Cliente> clientes) {
        this.clientes.addAll(clientes);
    }


    public ArrayList<Cliente> getClientes() {
        return clientes;
    }

    /**
     * Singleton que só permite uma instância do objeto ser criada, quando o atributo estático instancia tem o valor nulo.
     */
    public static RegistroDeClientes getInstancia() {
        if (instancia == null) {
            instancia = new RegistroDeClientes();
        }
        return instancia;
    }
}
