import java.io.File;
import java.io.PrintWriter;
import java.io.ObjectInputStream.GetField;
import java.util.Scanner;

import storage.NumericConverter;
import model2Alignment.AlignmentCreater;
import model2Alignment.Corpora;
import model2Alignment.Model;
import model2Alignment.NonLoadableCorpora;
import model2Alignment.Statistics;
import model2Alignment.TranslationAndAlignmentTableCreater;

public class Main {

	static String numericTargetFileName = "target";
	static String numericSourceFileName = "source";

	public static void main(String args[]) throws Exception {

		Scanner sc = new Scanner(System.in);
		// fun34(sc, 4);

		while (true) {
			System.out.println("Default numeric corpus files names : "
					+ numericSourceFileName + " - " + numericTargetFileName);

			System.out
					.println("Choose one of the following options:\n"
							+ "-1:  exit\n"
							+ "0 :  change the deafult corpus files names\n"
							+ "1 :  remove redundant punctioations from the corpus and delete too long sentences\n"
							+ "2 :  build a numeric model\n"
							+ "3 :  build a translation and alignment models\n"
							+ "4 :  build inversed translation and alignment models\n"
							+ "5 :  extract alignments from models (creating directed alignment file)\n"
							+ "6 :  extract alignments from inversed models (creating inversed alignment file)\n"
							+ "7 :  merge two alignment files\n"
							+ "8 :  create viewable corpus file sorted according to perblexity and sentence length difference distribution\n"
							+ "9 :  compute statistics about corpus sentences length differences\n"
							+ "10:  build bidirectional translation and alignment models\n"
							+ "11:  build bidirectional model on large corpus by splitting it\n"
							+ "12:  filter corpus according to perb. and sentence length dist. prob.");

			int cmd = Integer.parseInt(sc.nextLine());

			if (cmd == -1) {
				return;
			}

			if (cmd == 0) {
				System.out.print("Numeric corpus source file name: ");
				numericSourceFileName = sc.nextLine();
				System.out.print("Numeric corpus target file name: ");
				numericTargetFileName = sc.nextLine();
			} else if (cmd == 1) {
				System.out.print("Corpus source file path: ");
				String src = sc.nextLine();
				System.out.print("Corpus target file path : ");
				String tar = sc.nextLine();
				System.out
						.print("Output prefix : (output will be to [prefix]source.pun , [prefix]target.pun )");
				String out = sc.nextLine();
				System.out.print("Max source sentence length (in words : ");
				int maxSrc = Integer.parseInt(sc.nextLine());
				System.out.print("Max target sentence length (in words : ");
				int maxTar = Integer.parseInt(sc.nextLine());

				CorpusFilter.correctPuncAndFilter(src, tar, out + "source.pun",
						out + "target.pun", maxSrc, maxTar);

				System.out.println("Done removing redundant punctioations.");
			} else if (cmd == 2) {
				System.out
						.println("Enter the source file path , then target file path , then path prefix of the output:");
				String src = sc.nextLine();
				String tar = sc.nextLine();
				String out = sc.nextLine();
				storage.NumericConverter.buildNumbericModel(src, tar, out);
				System.out.println("Done bulding numeric model.");
			} else if (cmd == 3 || cmd == 4) {
				System.out
						.println("Enter the path of the numeric corpus directory , then path prefix of the output:");
				String path = sc.nextLine();
				String output = sc.nextLine();

				if (path.charAt(path.length() - 1) != '/')
					path += "/";

				int numOfWords = 0;
				int maxSourceLen = 0;
				int maxTargetLen = 0;
				if (cmd == 3) {
					numOfWords = NumericConverter
							.getWordsCountFromHashTableFile(path
									+ numericTargetFileName + ".hash");
					maxSourceLen = NumericConverter
							.getMaxSentenceLengthOfNumericOrLexicalFile(path
									+ numericSourceFileName + ".num");
					maxTargetLen = NumericConverter
							.getMaxSentenceLengthOfNumericOrLexicalFile(path
									+ numericTargetFileName + ".num");
				} else {
					numOfWords = NumericConverter
							.getWordsCountFromHashTableFile(path
									+ numericSourceFileName + ".hash");
					maxTargetLen = NumericConverter
							.getMaxSentenceLengthOfNumericOrLexicalFile(path
									+ numericSourceFileName + ".num");
					maxSourceLen = NumericConverter
							.getMaxSentenceLengthOfNumericOrLexicalFile(path
									+ numericTargetFileName + ".num");
				}

				System.out.println(numOfWords + " " + maxSourceLen + " "
						+ maxTargetLen);
				Statistics.maximumSourceSentenceLength = 50; // maxSourceLen;
				Statistics.maximumTargetSentenceLength = 50; // maxTargetLen;

				Statistics st = new Statistics(null, numOfWords);

				Corpora corpora = null;

				if (cmd == 3)
					corpora = new NonLoadableCorpora(new File(path
							+ numericSourceFileName + ".num"), new File(path
							+ numericTargetFileName + ".num"));
				else
					corpora = new NonLoadableCorpora(new File(path
							+ numericTargetFileName + ".num"), new File(path
							+ numericSourceFileName + ".num"));

				System.gc();

				TranslationAndAlignmentTableCreater creater = new TranslationAndAlignmentTableCreater(
						corpora, st);
				creater.buildTranslationAndAlignmentTables(output);

				System.gc();

				NumericConverter converter2 = null;
				if (cmd == 3)
					converter2 = new NumericConverter(path
							+ numericSourceFileName + ".hash", path
							+ numericTargetFileName + ".hash");
				else
					converter2 = new NumericConverter(path
							+ numericTargetFileName + ".hash", path
							+ numericSourceFileName + ".hash");

				System.out
						.println("Hash tables reloaded for writing readable translation table..");

				creater.storeReadableTranslationModel(st, output + "Readable",
						converter2);
				converter2.cleanConverter();
				converter2 = null;

				System.out.println("Model was built.");

			} else if (cmd == 5) {
				System.out
						.println("Enter the path of the models ( so models are [path]Trans.txt , [path]Align.txt )\n");
				String path = sc.nextLine();
				System.out
						.println("Enter the path of the numeric corpus directory");
				String corpusDir = sc.nextLine();
				if (corpusDir.charAt(corpusDir.length() - 1) != '/')
					corpusDir += "/";

				System.out
						.println("Enter the path of the output alignment file: ");
				String output = sc.nextLine();

				int maxSourceLen = NumericConverter
						.getMaxSentenceLengthOfNumericOrLexicalFile(path
								+ numericSourceFileName + ".num");
				int maxTargetLen = NumericConverter
						.getMaxSentenceLengthOfNumericOrLexicalFile(path
								+ numericTargetFileName + ".num");

				System.out.println("Loading models..");
				Model model = new Model(null, path, maxSourceLen + 2,
						maxTargetLen + 2);
				System.out.println("Loading cprpus..");
				Corpora corpora = new NonLoadableCorpora(new File(corpusDir
						+ numericSourceFileName + ".num"), new File(corpusDir
						+ numericTargetFileName + ".num"));
				System.out.println("Corpus loaded");
				NumericConverter converter = new NumericConverter(corpusDir
						+ numericSourceFileName + ".hash", corpusDir
						+ numericTargetFileName + ".hash");
				System.out.println("Numeric Converter loaded");
				AlignmentCreater creator = new AlignmentCreater(corpora, model);
				creator.createAlignmentFile(output, converter);
				System.out.println("Done creating alignment file.");
				converter = null;
				corpora = null;
				model = null;
				creator = null;

			} else if (cmd == 6) {
				System.out
						.println("Enter the path of the inversed models ( so models are [path]Trans.txt , [path]Align.txt )\n");
				String path = sc.nextLine();
				System.out
						.println("Enter the path of the numeric corpus directory");
				String corpusDir = sc.nextLine();
				if (corpusDir.charAt(corpusDir.length() - 1) != '/')
					corpusDir += "/";
				System.out
						.println("Enter the path of the output inversed alignment file: ");
				String output = sc.nextLine();
				System.out.println("Loading models..");

				int maxTargetLen = NumericConverter
						.getMaxSentenceLengthOfNumericOrLexicalFile(path
								+ numericSourceFileName + ".num");
				int maxSourceLen = NumericConverter
						.getMaxSentenceLengthOfNumericOrLexicalFile(path
								+ numericTargetFileName + ".num");

				Model model = new Model(null, path, maxSourceLen + 2,
						maxTargetLen + 2);
				System.out.println("Loading corpus..");
				Corpora corpora = new NonLoadableCorpora(new File(corpusDir
						+ numericTargetFileName + ".num"), new File(corpusDir
						+ numericSourceFileName + ".num"));
				System.out.println("Corpus inversely loaded");
				NumericConverter converter = new NumericConverter(corpusDir
						+ numericTargetFileName + ".hash", corpusDir
						+ numericSourceFileName + ".hash");
				System.out.println("Numeric Converter inversely loaded");
				AlignmentCreater creator = new AlignmentCreater(corpora, model);
				creator.createAlignmentFile(output, converter);
				System.out.println("Done creating inversed alignment file.");
			}

			else if (cmd == 7) {
				System.out
						.println("Enter the path of the direct alignment model file , then the path of the inversed , then output path\n");
				String path1 = sc.nextLine();
				String path2 = sc.nextLine();
				String output = sc.nextLine();
				AlignmentCreater.mergeAlignmentFiles(path1, path2, output);
				System.out.println("Merged successfully.");
			} else if (cmd == 8) {
				System.out
						.println("Enter the path of the numeric corpus directory");
				String copusDir = sc.nextLine();
				System.out
						.println("Enter the full path of the alignment probabilities ([MergedAlignmentFilePath] + '.probs')");
				String alPath = sc.nextLine();
				System.out.println("Enter the path of the output file");
				String output = sc.nextLine();

				if (copusDir.charAt(copusDir.length() - 1) != '/')
					copusDir += "/";

				System.out.println("Loading numeric converter..");
				NumericConverter converter = new NumericConverter(copusDir
						+ numericSourceFileName + ".hash", copusDir
						+ numericTargetFileName + ".hash");

				System.out.println("Working..");
				CorpusFilter.sortByPerp(copusDir + numericSourceFileName
						+ ".num", copusDir + numericTargetFileName + ".num",
						alPath, output, converter);

				System.out.println("Sorted successfully.");
			} else if (cmd == 9) {
				System.out
						.println("Enter the path of the numeric corpus directory");
				String copusDir = sc.nextLine();
				System.out
						.println("Enter the full path of the alignment probabilities ([MergedAlignmentFilePath] + '.probs')");
				String alPath = sc.nextLine();

				if (copusDir.charAt(copusDir.length() - 1) != '/')
					copusDir += "/";

				NumericConverter converter = new NumericConverter(copusDir
						+ numericSourceFileName + ".hash", copusDir
						+ numericTargetFileName + ".hash");

				while (true) {
					System.out
							.println("Enter the maximum perb that would enter the computation. Typically in range [0.5-5]\n"
									+ "-1 to exit");
					double maxPerb = Double.parseDouble(sc.nextLine());
					System.out
							.println("Enter the weight of the sentences length distribution [0-1]");
					double weight = Double.parseDouble(sc.nextLine());
					System.out.println("Enter the path of the output file");
					String out = sc.nextLine();
					CorpusFilter.sortByPerpAndSentenceLengthDistribution(
							copusDir + numericSourceFileName + ".num", copusDir
									+ numericTargetFileName + ".num", alPath,
							maxPerb, weight, out, converter);
				}
			} else if (cmd == 10) {
				System.out
						.println("Enter the path of the numeric corpus directory , then path prefix of the output:");

				String path = sc.nextLine();
				String output = sc.nextLine();

				if (path.charAt(path.length() - 1) != '/')
					path += "/";

				createBidirectionalModels(path, output);
			} else if (cmd == 11) {
				System.out
						.println("Enter the path of the LEXICAL Punctioation-Corrected corpus source , them target files , then path prefix of the output"
								+ "\n then the size of each part (in sentences):"
								+ "\n then the firs part you want to start with (typically 0 , unless you want to resume a previous work on the same corpus)");
				String source = sc.nextLine();
				String target = sc.nextLine();

				String output = sc.nextLine();
				int chunkSize = Integer.parseInt(sc.nextLine());
				int start = Integer.parseInt(sc.nextLine());

				int corpusCount = 0;
				Scanner csc1 = new Scanner(new File(source), "UTF-8");
				Scanner csc2 = new Scanner(new File(target), "UTF-8");

				PrintWriter wr1 = null, wr2 = null;
				if (start == 0) {
					wr1 = new PrintWriter(output + "lex_source" + corpusCount,
							"UTF-8");
					wr2 = new PrintWriter(output + "lex_target" + corpusCount,
							"UTF-8");
				}
				int writed = 0;

				while (csc1.hasNextLine() && csc2.hasNextLine()) {
					if (writed == chunkSize) {
						if (corpusCount >= start) {
							wr1.close();
							wr2.close();

							NumericConverter.buildNumbericModel(output
									+ "lex_source" + corpusCount, output
									+ "lex_target" + corpusCount, output
									+ "num_" + corpusCount + "_");
						}

						corpusCount++;
						writed = 0;

						if (corpusCount >= start) {
							wr1 = new PrintWriter(output + "lex_source"
									+ corpusCount, "UTF-8");
							wr2 = new PrintWriter(output + "lex_target"
									+ corpusCount, "UTF-8");
						}
					}

					String s1 = csc1.nextLine(), s2 = csc2.nextLine();

					if (corpusCount >= start) {
						wr1.println(s1);
						wr2.println(s2);
					}
					writed++;
				}

				if (corpusCount >= start) {
					wr1.close();
					wr2.close();
					NumericConverter.buildNumbericModel(output + "lex_source"
							+ corpusCount, output + "lex_target" + corpusCount,
							output + "num_" + corpusCount + "_");
				}

				System.out.println("Corpus divided to " + (corpusCount + 1)
						+ " subcorpus(es) and those were numerized.");

				for (int i = start; i <= corpusCount; i++) {
					System.out
							.println("=========================================================");
					System.out.println("Processing numeric subcorpus " + i);
					createBidirectionalModels(output + "num_" + i + "_", output
							+ "model_" + i + "_");
				}

				System.out
						.println("Enter the path of the directory in which to put the final numeric corpus and merged OUTPUT alignment file");
				String finalfolder = sc.nextLine();
				if (finalfolder.charAt(finalfolder.length() - 1) != '/')
					finalfolder += "";

				PrintWriter alwr = new PrintWriter(finalfolder
						+ "FinalAlignment" + ".al", "UTF-8");
				PrintWriter alprobwr = new PrintWriter(finalfolder
						+ "FinalAlignment" + ".probs", "UTF-8");
				int count = 0;
				System.out.println("Creating final merged alignment file..");
				for (int i = 0; i <= corpusCount; i++) {
					try {
						Scanner ssc = new Scanner(new File(output + "model_"
								+ i + "_AlignmentMerged"), "UTF-8");
						Scanner sscp = new Scanner(new File(output + "model_"
								+ i + "_AlignmentMerged.probs"), "UTF-8");
						while (ssc.hasNextLine()) {
							String str = ssc.nextLine();
							String pro = sscp.nextLine();
							alwr.println(str);
							alprobwr.println(pro);
							count++;
						}
						ssc.close();
						System.out.println("Processed " + output + "model_" + i
								+ "_AlignmentMerged['' | '.probs'])");
					} catch (Exception ee) {
						System.out.println("Warning: File " + output + "model_"
								+ i + "_AlignmentMerged['' | '.probs']"
								+ " Not found.");
					}
				}
				System.out.println(count + " alignment-lines writed");
				alwr.close();
				alprobwr.close();

				System.out
						.println("Creating a unified hash table and numeric corpus..");
				NumericConverter
						.buildNumbericModel(source, target, finalfolder);

			}

			else if (cmd == 12) {
				System.out.println("Enter the path of the numeric corpus directory\n"
						+ "then the full path of the alignment probabilities ([MergedAlignmentFilePath] + '.probs')\n"
						+ "then the maximum perb that would enter the computation of length dist. Typically in range [0.5-5]\n"
						+ "then the weight of the sentences length dist. [0-1]\n"
						+ "then the path of the output directory\n"
						+ "then the cutOff perb. (the one to filter according , typically [2.5-5]");
				
				String copusDir = sc.nextLine();
				String alPath = sc.nextLine();
				double maxPerb = Double.parseDouble(sc.nextLine());
				double weight = Double.parseDouble(sc.nextLine());
				String out = sc.nextLine();
				double cutOffPerb = Double.parseDouble(sc.nextLine());
				
				if (copusDir.charAt(copusDir.length() - 1) != '/')
					copusDir += "/";
				
				if (out.charAt(out.length() - 1) != '/')
					out += "/";
				
				NumericConverter converter = new NumericConverter(copusDir	+ numericSourceFileName + ".hash", copusDir
						+ numericTargetFileName + ".hash");
				
				System.out.println("Working..");
				CorpusFilter.filterCorpusAccordingToPerpAndSentenceLengthDistribution(
						copusDir + numericSourceFileName + ".num",
						copusDir + numericTargetFileName + ".num",
						alPath,cutOffPerb,	maxPerb, weight, out, converter);
		
				System.out.println("Finished");

			}

			System.out.println("-----------------------------------------\n");
		}

	}

	public static void createBidirectionalModels(String path, String output)
			throws Exception {

		int numOfTargetWords = 0;
		int numOfSourceWords = 0;
		int maxSourceLen = 0;
		int maxTargetLen = 0;

		numOfSourceWords = NumericConverter.getWordsCountFromHashTableFile(path
				+ numericSourceFileName + ".hash");
		numOfTargetWords = NumericConverter.getWordsCountFromHashTableFile(path
				+ numericTargetFileName + ".hash");

		maxSourceLen = NumericConverter
				.getMaxSentenceLengthOfNumericOrLexicalFile(path
						+ numericSourceFileName + ".num");
		maxTargetLen = NumericConverter
				.getMaxSentenceLengthOfNumericOrLexicalFile(path
						+ numericTargetFileName + ".num");

		System.out.println("# source words=" + numOfSourceWords + "\n"
				+ "# target words=" + numOfTargetWords + "\n"
				+ "# maximum source sentence length=" + maxSourceLen + "\n"
				+ "# maximum target sentence length=" + maxTargetLen);

		Statistics.maximumSourceSentenceLength = maxSourceLen + 2;
		Statistics.maximumTargetSentenceLength = maxTargetLen + 2;

		Statistics st = new Statistics(null, numOfTargetWords);

		Corpora corpora = new NonLoadableCorpora(new File(path
				+ numericSourceFileName + ".num"), new File(path
				+ numericTargetFileName + ".num"));

		TranslationAndAlignmentTableCreater creater = new TranslationAndAlignmentTableCreater(
				corpora, st);
		creater.buildTranslationAndAlignmentTables(output + "modelDirect");

		System.out.println("Direct model was built.");

		st = null;
		System.gc();

		Model model = new Model(null, output + "modelDirect", maxSourceLen + 2,
				maxTargetLen + 2);
		// System.out.println("Loading Numeric Converter..");
		NumericConverter converter = null;// new NumericConverter(path +
											// numericSourceFileName + ".hash",
											// path + numericTargetFileName +
											// ".hash");
		System.out.println("Creating alignment file");

		AlignmentCreater creator = new AlignmentCreater(corpora, model);
		creator.createAlignmentFile(output + "AlignmentDirect", converter);
		System.out.println("Done creating alignment file.");

		converter = null;
		corpora = null;
		model = null;
		creator = null;
		System.gc();

		System.out.println("Starting building inversed model..");

		Statistics.maximumSourceSentenceLength = maxTargetLen + 2;
		Statistics.maximumTargetSentenceLength = maxSourceLen + 2;

		st = new Statistics(null, numOfSourceWords);

		corpora = new NonLoadableCorpora(new File(path + numericTargetFileName
				+ ".num"), new File(path + numericSourceFileName + ".num"));

		creater = new TranslationAndAlignmentTableCreater(corpora, st);
		creater.buildTranslationAndAlignmentTables(output + "modelInversed");

		System.out.println("Inversed model was built.");

		st = null;
		System.gc();

		model = new Model(null, output + "modelInversed", maxTargetLen + 2,
				maxSourceLen + 2);
		// System.out.println("Loading Numeric Converter..");
		converter = null;// new NumericConverter(path + numericTargetFileName +
							// ".hash", path + numericSourceFileName + ".hash");
		System.out.println("Creating alignment file");

		creator = new AlignmentCreater(corpora, model);
		creator.createAlignmentFile(output + "AlignmentInversed", converter);
		System.out.println("Done creating alignment file.");

		converter = null;
		corpora = null;
		model = null;
		creator = null;
		System.gc();

		System.out.println("Merging the two alignment files..");
		AlignmentCreater.mergeAlignmentFiles(output + "AlignmentDirect.al",
				output + "AlignmentInversed.al", output + "AlignmentMerged");

		System.out.println("Merged successfully.");
		System.out.println("Finished.");

	}

}
