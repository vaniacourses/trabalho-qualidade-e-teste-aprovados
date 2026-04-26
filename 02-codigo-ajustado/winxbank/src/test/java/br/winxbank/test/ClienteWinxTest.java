package br.winxbank.test;

import br.winxbank.sistemabancario.Conta;
import br.winxbank.sistemabancario.Movimentacao;
import br.winxbank.sistemaclientes.ClienteWinx;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ClienteWinxTest {
	
	@Mock
    Conta contaMock;

    private ClienteWinx cliente;

    @BeforeEach
    void setUp() {
    	MockitoAnnotations.openMocks(this);
        cliente = new ClienteWinx("Dani", "12345678900", 10);
    }

    @Test
    void deveRetornarPontosDeCompraCorretamente() {
        assertEquals(10, cliente.getPontosDeCompra());
    }

    @Test
    void deveIncrementarPontosDeCompra() {
        cliente.obterPontosDeCompra();
        assertEquals(11, cliente.getPontosDeCompra());
    }

    @Test
    void deveConverterPontosEmSaldo() {
    	cliente.converterPontosEmSaldo(contaMock);

        verify(contaMock).setSaldo(30f);
        assertEquals(0, cliente.getPontosDeCompra());
    }

    @Test
    void deveRegistrarMovimentacaoAoConverterPontos() {

        cliente.converterPontosEmSaldo(contaMock);

        ArgumentCaptor<Movimentacao> captor = ArgumentCaptor.forClass(Movimentacao.class);

        verify(contaMock).setExtrato(captor.capture());

        Movimentacao movimentacao = captor.getValue();

        assertEquals(30f, movimentacao.getDinheiroMovimentado());
        assertEquals(Movimentacao.TipoDaMovimentacao.ENTRADA, movimentacao.getTipoDaMovimentacao());
    }

    @Test
    void deveZerarPontosAposConversao() {

        cliente.converterPontosEmSaldo(contaMock);

        assertEquals(0, cliente.getPontosDeCompra());
    }
}