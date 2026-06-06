package com.prison.service.psych;

import com.prison.dto.psych.PsychScaleDTO;
import com.prison.entity.psych.*;
import com.prison.repository.psych.*;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class PsychScaleService {

    private final PsychScaleRepository scaleRepository;
    private final PsychScaleQuestionRepository questionRepository;
    private final PsychScaleOptionRepository optionRepository;
    private final PsychScaleFactorRepository factorRepository;

    @PostConstruct
    @Transactional
    public void initBuiltInScales() {
        initSCL90();
        initSDS();
        initSAS();
        initMMPI();
    }

    public List<PsychScale> listAllScales() {
        return scaleRepository.findAll();
    }

    public Optional<PsychScale> getScaleById(Long id) {
        return scaleRepository.findById(id);
    }

    public Optional<PsychScale> getScaleByCode(String code) {
        return scaleRepository.findTopByCodeOrderByVersionDesc(code);
    }

    public List<PsychScaleQuestion> getQuestionsByScaleId(Long scaleId) {
        return questionRepository.findByScaleIdOrderByQuestionNo(scaleId);
    }

    public List<PsychScaleOption> getOptionsByQuestionId(Long questionId) {
        return optionRepository.findByQuestionIdOrderByOptionNo(questionId);
    }

    public List<PsychScaleFactor> getFactorsByScaleId(Long scaleId) {
        return factorRepository.findByScaleId(scaleId);
    }

    @Transactional
    public PsychScale createScale(PsychScaleDTO dto) {
        PsychScale scale = new PsychScale();
        scale.setName(dto.getName());
        scale.setCode(dto.getCode());
        scale.setDescription(dto.getDescription());
        scale.setQuestionCount(dto.getQuestionCount() != null ? dto.getQuestionCount() : 0);
        scale.setScaleType(dto.getScaleType());
        scale.setScoringRule(dto.getScoringRule());
        return scaleRepository.save(scale);
    }

    @Transactional
    public PsychScale updateScale(Long id, PsychScaleDTO dto) {
        return scaleRepository.findById(id).map(scale -> {
            scale.setName(dto.getName());
            scale.setDescription(dto.getDescription());
            scale.setScaleType(dto.getScaleType());
            scale.setScoringRule(dto.getScoringRule());
            scale.setVersion(scale.getVersion() + 1);
            return scaleRepository.save(scale);
        }).orElse(null);
    }

    @Transactional
    public void deleteScale(Long id) {
        List<PsychScaleQuestion> questions = questionRepository.findByScaleIdOrderByQuestionNo(id);
        for (PsychScaleQuestion q : questions) {
            optionRepository.deleteByQuestionId(q.getId());
        }
        questionRepository.deleteAll(questions);
        factorRepository.deleteByScaleId(id);
        scaleRepository.deleteById(id);
    }

    @Transactional
    public PsychScaleQuestion addQuestion(Long scaleId, PsychScaleQuestion question) {
        question.setScaleId(scaleId);
        PsychScaleQuestion saved = questionRepository.save(question);
        scaleRepository.findById(scaleId).ifPresent(scale -> {
            scale.setQuestionCount(scale.getQuestionCount() + 1);
            scale.setVersion(scale.getVersion() + 1);
            scaleRepository.save(scale);
        });
        return saved;
    }

    @Transactional
    public PsychScaleOption addOption(Long questionId, PsychScaleOption option) {
        option.setQuestionId(questionId);
        return optionRepository.save(option);
    }

    @Transactional
    public PsychScaleFactor addFactor(Long scaleId, PsychScaleFactor factor) {
        factor.setScaleId(scaleId);
        return factorRepository.save(factor);
    }

    private void initSCL90() {
        Optional<PsychScale> existing = scaleRepository.findByCode("SCL90");
        if (existing.isPresent()) return;

        String[] questions = {
            "头痛", "神经过敏，心中不踏实", "头脑中有不必要的想法或字句盘旋", "头昏或昏倒",
            "对异性的兴趣减退", "对旁人责备求全", "感到别人能控制您的思想", "责怪别人制造麻烦",
            "忘性大", "担心自己的衣饰整齐及仪态的端正", "容易烦恼和激动", "胸痛",
            "害怕空旷的场所或街道", "感到自己的精力下降，活动减慢", "想结束自己的生命",
            "听到旁人听不到的声音", "发抖", "感到大多数人都不可信任", "胃口不好",
            "容易哭泣", "同异性相处时感到害羞不自在", "感到受骗、中了圈套或有人想抓住您",
            "无缘无故地突然感到害怕", "自己不能控制地大发脾气", "怕单独出门",
            "经常责怪自己", "腰痛", "感到难以完成任务", "感到孤独", "感到苦闷",
            "过分担忧", "对事物不感兴趣", "感到害怕", "您的感情容易受到伤害",
            "旁人能知道您的私下想法", "感到别人不理解您、不同情您",
            "感到人们对您不友好、不喜欢您", "做事必须做得很慢以保证做得正确",
            "心跳得很厉害", "恶心或胃部不舒服", "感到比不上他人", "肌肉酸痛",
            "感到有人在监视您、谈论您", "难以入睡", "做事必须反复检查", "难以作出决定",
            "怕乘电车、公共汽车、地铁或火车", "呼吸有困难", "一阵阵发冷或发热",
            "因为感到害怕而避开某些东西、场合或活动", "脑子变空了", "身体发麻或刺痛",
            "喉咙有梗塞感", "感到前途没有希望", "不能集中注意", "感到身体的某一部分软弱无力",
            "感到紧张或容易紧张", "感到手或脚发重", "想到死亡的事", "吃得太多",
            "当别人看着您或谈论您时感到不自在", "有一些不属于您自己的想法",
            "有想打人或伤害他人的冲动", "醒得太早", "必须反复洗手、点数目或触摸某些东西",
            "睡得不稳不深", "有想摔坏或破坏东西的冲动", "有一些别人没有的想法或念头",
            "感到对别人神经过敏", "在商店或电影院等人多的地方感到不自在",
            "感到任何事情都很困难", "一阵阵恐惧或惊恐", "感到在公共场合吃东西很不舒服",
            "经常与人争论", "单独一人时神经很紧张", "别人对您的成绩没有作出恰当的评价",
            "即使和别人在一起也感到孤单", "感到坐立不安、心神不定", "感到自己没有什么价值",
            "感到熟悉的东西变成陌生或不像是真的", "大叫或摔东西", "害怕会在公共场合昏倒",
            "感到别人想占您的便宜", "为一些有关“性”的想法而很苦恼",
            "您认为应该因为自己的过错而受到惩罚", "感到要赶快把事情做完",
            "感到自己的身体有严重问题", "从未感到和其他人很亲近", "感到自己有罪",
            "感到自己的脑子有毛病"
        };

        String[] factors = {
            "躯体化", "强迫", "人际敏感", "抑郁", "焦虑", "敌对", "恐怖", "偏执", "精神病性"
        };

        String[] factorQuestions = {
            "1,4,12,27,40,42,48,49,52,53,56,58",
            "3,9,10,28,38,45,46,51,55,65",
            "6,21,34,36,37,41,61,69,73",
            "5,14,15,20,22,26,29,30,31,32,54,71,79",
            "2,17,23,33,39,57,72,78,80,86",
            "11,24,63,67,74,81",
            "13,25,47,50,70,75,82",
            "8,18,43,68,76,83",
            "7,16,35,62,77,84,85,87,88,90"
        };

        PsychScale scale = new PsychScale();
        scale.setName("SCL-90症状自评量表");
        scale.setCode("SCL90");
        scale.setDescription("90项症状自评量表，用于评估心理健康状况");
        scale.setQuestionCount(questions.length);
        scale.setScaleType("SCL90");
        scale.setScoringRule("各因子分=对应题目平均分，总分=所有题目得分之和");
        PsychScale savedScale = scaleRepository.save(scale);
        Long scaleId = savedScale.getId();

        for (int i = 0; i < questions.length; i++) {
            PsychScaleQuestion q = new PsychScaleQuestion();
            q.setScaleId(scaleId);
            q.setQuestionNo(i + 1);
            q.setQuestionText(questions[i]);
            q.setQuestionType("SINGLE");
            q.setReverseScoring(false);
            PsychScaleQuestion savedQ = questionRepository.save(q);

            String[] optionTexts = {"没有", "很轻", "中度", "偏重", "严重"};
            for (int j = 0; j < optionTexts.length; j++) {
                PsychScaleOption opt = new PsychScaleOption();
                opt.setQuestionId(savedQ.getId());
                opt.setOptionNo(j + 1);
                opt.setOptionText(optionTexts[j]);
                opt.setScore(j + 1);
                optionRepository.save(opt);
            }
        }

        for (int i = 0; i < factors.length; i++) {
            PsychScaleFactor f = new PsychScaleFactor();
            f.setScaleId(scaleId);
            f.setFactorName(factors[i]);
            f.setQuestionNos(factorQuestions[i]);
            f.setDescription(factors[i] + "因子");
            factorRepository.save(f);
        }
    }

    private void initSDS() {
        Optional<PsychScale> existing = scaleRepository.findByCode("SDS");
        if (existing.isPresent()) return;

        String[] questions = {
            "我感到情绪沮丧，郁闷", "我感到早晨心情最好", "我要哭或想哭",
            "我夜间睡眠不好", "我吃饭像平时一样多", "我的性功能正常",
            "我感到体重减轻", "我为便秘烦恼", "我的心跳比平时快",
            "我无故感到疲劳", "我的头脑像往常一样清楚", "我做事情像平时一样不感到困难",
            "我坐卧不安，难以保持平静", "我对未来感到有希望", "我比平时更容易激怒",
            "我觉得决定什么事很容易", "我感到自己是有用的和不可缺少的人",
            "我的生活很有意义", "假若我死了别人会过得更好", "我仍旧喜爱自己平时喜爱的东西"
        };

        int[] reverseScores = {0, 1, 0, 0, 1, 1, 0, 0, 0, 0, 1, 1, 0, 1, 0, 1, 1, 1, 0, 1};

        PsychScale scale = new PsychScale();
        scale.setName("SDS抑郁自评量表");
        scale.setCode("SDS");
        scale.setDescription("抑郁自评量表，用于评估抑郁程度");
        scale.setQuestionCount(questions.length);
        scale.setScaleType("SDS");
        scale.setScoringRule("粗分=各题得分之和，标准分=粗分×1.25取整");
        PsychScale savedScale = scaleRepository.save(scale);
        Long scaleId = savedScale.getId();

        for (int i = 0; i < questions.length; i++) {
            PsychScaleQuestion q = new PsychScaleQuestion();
            q.setScaleId(scaleId);
            q.setQuestionNo(i + 1);
            q.setQuestionText(questions[i]);
            q.setQuestionType("SINGLE");
            q.setReverseScoring(reverseScores[i] == 1);
            PsychScaleQuestion savedQ = questionRepository.save(q);

            String[] optionTexts = {"没有或很少时间", "小部分时间", "相当多时间", "绝大部分或全部时间"};
            for (int j = 0; j < optionTexts.length; j++) {
                PsychScaleOption opt = new PsychScaleOption();
                opt.setQuestionId(savedQ.getId());
                opt.setOptionNo(j + 1);
                opt.setOptionText(optionTexts[j]);
                opt.setScore(j + 1);
                optionRepository.save(opt);
            }
        }
    }

    private void initSAS() {
        Optional<PsychScale> existing = scaleRepository.findByCode("SAS");
        if (existing.isPresent()) return;

        String[] questions = {
            "我觉得比平常容易紧张和着急", "我无缘无故地感到害怕", "我容易心里烦乱或觉得惊恐",
            "我觉得我可能将要发疯", "我觉得一切都很好，也不会发生什么不幸",
            "我手脚发抖打颤", "我因为头痛、颈痛和背痛而苦恼", "我感觉容易衰弱和疲乏",
            "我觉得心平气和，并且容易安静坐着", "我觉得心跳得很快",
            "我因为一阵阵头晕而苦恼", "我有晕倒发作，或觉得要晕倒似的",
            "我吸气呼气都感到很容易", "我的手脚麻木和刺痛",
            "我因为胃痛和消化不良而苦恼", "我常常要小便",
            "我的手脚常常是干燥温暖的", "我脸红发热",
            "我容易入睡并且一夜睡得很好", "我做恶梦"
        };

        int[] reverseScores = {0, 0, 0, 0, 1, 0, 0, 0, 1, 0, 0, 0, 1, 0, 0, 0, 1, 0, 1, 0};

        PsychScale scale = new PsychScale();
        scale.setName("SAS焦虑自评量表");
        scale.setCode("SAS");
        scale.setDescription("焦虑自评量表，用于评估焦虑程度");
        scale.setQuestionCount(questions.length);
        scale.setScaleType("SAS");
        scale.setScoringRule("粗分=各题得分之和，标准分=粗分×1.25取整");
        PsychScale savedScale = scaleRepository.save(scale);
        Long scaleId = savedScale.getId();

        for (int i = 0; i < questions.length; i++) {
            PsychScaleQuestion q = new PsychScaleQuestion();
            q.setScaleId(scaleId);
            q.setQuestionNo(i + 1);
            q.setQuestionText(questions[i]);
            q.setQuestionType("SINGLE");
            q.setReverseScoring(reverseScores[i] == 1);
            PsychScaleQuestion savedQ = questionRepository.save(q);

            String[] optionTexts = {"没有或很少时间", "小部分时间", "相当多时间", "绝大部分或全部时间"};
            for (int j = 0; j < optionTexts.length; j++) {
                PsychScaleOption opt = new PsychScaleOption();
                opt.setQuestionId(savedQ.getId());
                opt.setOptionNo(j + 1);
                opt.setOptionText(optionTexts[j]);
                opt.setScore(j + 1);
                optionRepository.save(opt);
            }
        }
    }

    private void initMMPI() {
        Optional<PsychScale> existing = scaleRepository.findByCode("MMPI");
        if (existing.isPresent()) return;

        String[] mmpiQuestions = {
            "我喜欢看机械方面的杂志", "我的胃口很好", "我早上起来的时候，多半觉得睡眠充足、头脑清醒",
            "我想我会喜欢图书管理员的工作", "我很容易被吵醒", "我喜欢看报纸上的犯罪新闻",
            "我的手脚经常是很暖和的", "我的日常生活中，充满了使我感兴趣的事情",
            "我现在工作（学习）的能力，和从前差不多", "我的喉咙里总好像有一块东西堵着似的",
            "我觉得一个人最好是去推想所有的人的行为", "我相信有人在暗地里追踪我",
            "我似乎和周围的人一样精明能干", "我觉得我有过某种不可告人的坏念头",
            "我喜欢侦探小说或神秘小说", "我总觉得人生是有价值的",
            "我认为一个人决不该承认自己做错了事", "我想大多数人会为了得到较多的报酬而撒谎",
            "我常做一些使我害怕的梦", "我喜欢修理自行车",
            "我希望能像别人那样快乐", "我的性方面有问题",
            "我在学校读书时，常因表现不好而被老师批评", "我觉得大多数人之所以能爬上高位，是因为他们认识某些人",
            "我喜欢看爱情小说", "我觉得自己的生活是失败的",
            "我相信自己的生活和大多数人一样丰富多彩", "我喜欢谈论性方面的事情",
            "我在童年时，曾有一段时间干过小偷小摸的事", "我喜欢看报纸上的社论",
            "我喜欢在热闹的地方游玩", "我愿意做建筑工人或园艺工人",
            "我常感到自己的生活杂乱无章", "我觉得大多数人对于性的事谈得太多了",
            "我很容易对人产生同情心", "我认为大多数人会利用别人的可怜处境去占便宜",
            "我喜欢看侦探电影", "我的宗教信仰非常虔诚",
            "我在小时候，曾因为不守规矩而受到严厉的惩罚",
            "我觉得大多数人对于宗教方面的事情，都是口是心非的",
            "我愿意当一个新闻记者", "我有时候会无缘无故地觉得浑身发热",
            "我喜欢看有关于科学发明的书报", "我的家庭生活总是使我感到不满意",
            "我很容易对别人产生妒忌心", "我喜欢看体育比赛",
            "我愿意当一个农民", "有时候我真想摔东西",
            "我喜欢去参观美术馆和历史博物馆", "我觉得我是一个容易感情用事的人",
            "我相信自己是一个被人诅咒的人", "我的父母常常反对我所做的事",
            "我喜欢玩电子游戏", "我觉得我的记忆力似乎还不错",
            "我对于性的问题，感觉到有羞耻感", "我很想参加赛车比赛",
            "我常常觉得好像有什么可怕的事情将要发生",
            "我喜欢看报纸上的连环漫画", "我愿意做一个空军飞行员",
            "我觉得我有一些奇异的想法", "当我必须当众讲话时，我会感到非常紧张",
            "我喜欢打猎或钓鱼", "我相信有鬼神",
            "我的身体状况使我经常感到不舒服", "我喜欢看战争题材的电影",
            "我喜欢读历史书", "我觉得自己的脑子有问题",
            "我愿意当一名警察", "我相信世界上没有真正爱我的人",
            "我喜欢看关于探险的书", "我很容易疲倦",
            "我愿意当一个科学家", "我有过很奇怪的宗教体验",
            "我的皮肤好像特别敏感", "我喜欢看恐怖电影",
            "我愿意当一个演员", "我觉得我的生活方式完全是由别人决定的",
            "我喜欢集邮或收集其他东西", "我有时候会想到一些坏得说不出口的事",
            "我愿意当一个护士", "我觉得大多数人都是不可信赖的",
            "我喜欢看神话故事", "我有过想要自杀的念头",
            "我愿意当一个工程师", "我相信有人想要害我",
            "我喜欢看幽默笑话", "我觉得我的前途一片黑暗",
            "我愿意当一个音乐家", "我有时会听到一些别人听不到的声音",
            "我喜欢看科学幻想小说", "我觉得自己是一个没用的人",
            "我愿意当一个作家", "我相信我有某种超自然的能力",
            "我喜欢看人物传记", "我有时候觉得自己不像自己了",
            "我愿意当一个医生", "我觉得我的灵魂有一天会离开我的身体",
            "我喜欢看关于心理学的书", "我经常感到时间不够用",
            "我相信努力就会有回报"
        };

        PsychScale scale = new PsychScale();
        scale.setName("MMPI明尼苏达多相人格简版");
        scale.setCode("MMPI");
        scale.setDescription("MMPI简版，" + mmpiQuestions.length + "道是/否题");
        scale.setQuestionCount(mmpiQuestions.length);
        scale.setScaleType("MMPI");
        scale.setScoringRule("是/否作答，各临床量表计分");
        PsychScale savedScale = scaleRepository.save(scale);
        Long scaleId = savedScale.getId();

        for (int i = 0; i < mmpiQuestions.length; i++) {
            PsychScaleQuestion q = new PsychScaleQuestion();
            q.setScaleId(scaleId);
            q.setQuestionNo(i + 1);
            q.setQuestionText(mmpiQuestions[i]);
            q.setQuestionType("YESNO");
            q.setReverseScoring(false);
            PsychScaleQuestion savedQ = questionRepository.save(q);

            PsychScaleOption optYes = new PsychScaleOption();
            optYes.setQuestionId(savedQ.getId());
            optYes.setOptionNo(1);
            optYes.setOptionText("是");
            optYes.setScore(1);
            optionRepository.save(optYes);

            PsychScaleOption optNo = new PsychScaleOption();
            optNo.setQuestionId(savedQ.getId());
            optNo.setOptionNo(2);
            optNo.setOptionText("否");
            optNo.setScore(0);
            optionRepository.save(optNo);
        }
    }
}
