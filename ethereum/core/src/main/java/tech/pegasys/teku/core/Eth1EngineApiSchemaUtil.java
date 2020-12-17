/*
 * Copyright 2020 ConsenSys AG.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 */

package tech.pegasys.teku.core;

import java.util.Collections;
import java.util.stream.Collectors;
import org.apache.tuweni.bytes.Bytes;
import org.apache.tuweni.bytes.Bytes32;
import org.apache.tuweni.units.bigints.UInt256;
import tech.pegasys.teku.datastructures.blocks.exec.Eth1Transaction;
import tech.pegasys.teku.datastructures.blocks.exec.ExecutableData;
import tech.pegasys.teku.exec.eth1engine.schema.Eth1TransactionDTO;
import tech.pegasys.teku.exec.eth1engine.schema.ExecutableDataDTO;
import tech.pegasys.teku.infrastructure.unsigned.UInt64;
import tech.pegasys.teku.ssz.SSZTypes.Bytes20;

public final class Eth1EngineApiSchemaUtil {

  public static ExecutableData parseExecutableDataDTO(ExecutableDataDTO executableDataDTO) {
    return new ExecutableData(
        Bytes32.fromHexString(executableDataDTO.getBlock_hash()),
        Bytes20.fromHexString(executableDataDTO.getCoinbase()),
        Bytes32.fromHexString(executableDataDTO.getState_root()),
        UInt64.valueOf(executableDataDTO.getGas_limit()),
        UInt64.valueOf(executableDataDTO.getGas_used()),
        Bytes32.fromHexString(executableDataDTO.getReceipt_root()),
        Bytes.fromBase64String(executableDataDTO.getLogs_bloom()),
        UInt64.valueOf(executableDataDTO.getDifficulty()),
        executableDataDTO.getTransactions() != null
            ? executableDataDTO.getTransactions().stream()
                .map(Eth1EngineApiSchemaUtil::parseEth1TransactionDTO)
                .collect(Collectors.toList())
            : Collections.emptyList());
  }

  public static ExecutableDataDTO getExecutableDataDTO(ExecutableData executableData) {
    return new ExecutableDataDTO(
        executableData.getBlock_hash().toHexString(),
        executableData.getCoinbase().toHexString(),
        executableData.getState_root().toHexString(),
        executableData.getGas_limit().bigIntegerValue(),
        executableData.getGas_used().bigIntegerValue(),
        executableData.getReceipt_root().toHexString(),
        executableData.getLogs_bloom().toBase64String(),
        executableData.getDifficulty().bigIntegerValue(),
        executableData.getTransactions().stream()
            .map(Eth1EngineApiSchemaUtil::getEth1TransactionDTO)
            .collect(Collectors.toList()));
  }

  public static Eth1Transaction parseEth1TransactionDTO(Eth1TransactionDTO eth1TransactionDTO) {
    return new Eth1Transaction(
        UInt64.valueOf(UInt256.fromHexString(eth1TransactionDTO.getNonce()).toBigInteger()),
        UInt256.fromHexString(eth1TransactionDTO.getGasPrice()),
        UInt64.valueOf(UInt256.fromHexString(eth1TransactionDTO.getGas()).toBigInteger()),
        Bytes20.fromHexString(eth1TransactionDTO.getTo()),
        UInt256.fromHexString(eth1TransactionDTO.getValue()),
        Bytes.fromHexString(eth1TransactionDTO.getInput()),
        UInt256.fromHexString(eth1TransactionDTO.getV()),
        UInt256.fromHexString(eth1TransactionDTO.getR()),
        UInt256.fromHexString(eth1TransactionDTO.getS()));
  }

  public static Eth1TransactionDTO getEth1TransactionDTO(Eth1Transaction eth1Transaction) {
    return new Eth1TransactionDTO(
        Bytes.ofUnsignedLong(eth1Transaction.getNonce().longValue()).toQuantityHexString(),
        eth1Transaction.getGas_price().toBytes().toQuantityHexString(),
        Bytes.ofUnsignedLong(eth1Transaction.getGas_limit().longValue()).toQuantityHexString(),
        eth1Transaction.getRecipient().toHexString(),
        eth1Transaction.getValue().toBytes().toQuantityHexString(),
        eth1Transaction.getInput().toHexString(),
        eth1Transaction.getV().toBytes().toQuantityHexString(),
        eth1Transaction.getR().toBytes().toQuantityHexString(),
        eth1Transaction.getS().toBytes().toQuantityHexString());
  }
}
